package com.chat.ichat.screens.message;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.chat.ichat.MessageController;
import com.chat.ichat.api.ApiError;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.bot.BotApi;
import com.chat.ichat.api.bot.PersistentMenu;
import com.chat.ichat.api.message.MessageApi;
import com.chat.ichat.api.message.MessageDataResponse;
import com.chat.ichat.api.user.UserApi;
import com.chat.ichat.api.user.UserResponse;
import com.chat.ichat.core.GsonProvider;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.AudioMessage;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.ImageMessage;
import com.chat.ichat.models.Message;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.screens.new_chat.AddContactUseCase;

import org.jivesoftware.smackx.chatstates.ChatState;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 06/08/16.
 */
public class MessagePresenter implements MessageContract.Presenter {

    private MessageContract.View messageView;
    private CompositeSubscription compositeSubscription;

    private MessageStore messageStore;
    private MessageController messageController;
    private ContactStore contactStore;
    private BotDetailsStore botDetailsStore;
    private UserApi userApi;
    private MessageApi messageApi;
    private AddContactUseCase addContactUseCase;

    private SendMessageUseCase sendMessageUseCase;
    private SendReadReceiptUseCase sendReadReceiptUseCase;

    public MessagePresenter(MessageStore messageStore, MessageController messageController, BotDetailsStore botDetailsStore, ContactStore contactStore, UserApi userApi,  BotApi botApi, MessageApi messageApi) {
        this.messageController = messageController;
        this.messageStore = messageStore;
        this.botDetailsStore = botDetailsStore;
        this.contactStore = contactStore;
        this.userApi = userApi;
        this.messageApi = messageApi;
        this.addContactUseCase = new AddContactUseCase(userApi, contactStore, botApi, botDetailsStore);
        sendReadReceiptUseCase = new SendReadReceiptUseCase(messageController, messageStore);
        sendMessageUseCase = new SendMessageUseCase(messageController, messageStore);
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void addContact(String userId) {
        ContactResult contactResult = new ContactResult();
        contactResult.setUserId(userId);
        contactResult.setAdded(true);
        Subscription subscription = addContactUseCase.execute(userId, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ApiError error = new ApiError(e);
                        messageView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(ContactResult contactResult) {
                        if(contactResult == null) {
                            messageView.showError("Error", "There was an error adding this contact.");
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void blockContact(String userId, boolean shouldBlock) {
        ContactResult contactResult = new ContactResult();
        contactResult.setUserId(userId);
        contactResult.setBlocked(shouldBlock);
        Observable<UserResponse> a;
        if(shouldBlock) {
            a = ApiManager.getContactApi().blockContact(userId);
        } else {
            a = ApiManager.getContactApi().unblockContact(userId);
        }
        Subscription subscription = a
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ApiError error = new ApiError(e);
                        messageView.showError(error.getTitle(), error.getMessage());
                    }

                    @Override
                    public void onNext(UserResponse userResponse) {
                        contactResult.setBlocked(shouldBlock);
                        contactStore.update(contactResult)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<ContactResult>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(ContactResult contactResult) {
                                        messageView.showContactBlockedSuccess(shouldBlock);
                                    }
                                });
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void loadContactDetails(String chatUserName) {
        Subscription subscription = contactStore.getContactByUserName(chatUserName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContactResult>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(ContactResult contactResult) {
                        messageView.setContactDetails(contactResult);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void loadMessages(String chatUserName) {
        Logger.d(this, "Loading chat messages: "+chatUserName);
        Subscription subscription = messageStore.getMessages(chatUserName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<MessageResult>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Logger.e(this, "load messages");
                    }

                    @Override
                    public void onNext(List<MessageResult> messageResults) {
                        messageView.displayMessages(messageResults);
                        if(chatUserName.toLowerCase().startsWith("o_")) {
                            Logger.d(this, "CHAT_USER_OFFICIAL");
//                            MessageResult messageResult = messageResults.get(messageResults.size()-1);
//                            Message message = GsonProvider.getGson().fromJson(messageResult.getMessage(), Message.class);
//                            messageView.showHidePersistentMenu(!(message.getQuickReplies() != null && message.getQuickReplies().size() > 0));
                            messageView.showHidePersistentMenu(messageResults.size()==0);
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void loadKeyboard(String chatId) {
        Subscription subscription= botDetailsStore.getMenu(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<PersistentMenu>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        messageView.setKeyboardType(false);
                    }

                    @Override
                    public void onNext(List<PersistentMenu> persistentMenus) {
                        Logger.d(this, "PM "+persistentMenus.toString());
                        if(persistentMenus!=null && !persistentMenus.isEmpty()) {
                            messageView.initBotMenu(persistentMenus);
                            messageView.setKeyboardType(true);
                        } else {
                            messageView.setKeyboardType(false);
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void updateMessageRead(MessageResult result) {
        result.setMessageStatus(MessageResult.MessageStatus.SEEN);
        Subscription subscription = messageStore.updateMessage(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messageResult -> {
                    sendReadReceiptUseCase.execute(result).subscribe(isReceiptSent -> {
                        // is_receipt_sent updated if true
                    });
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void sendTextMessage(String toId, String fromId, String message) {
        MessageResult result = new MessageResult(toId, fromId, message);
        result.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);

        Subscription subscription = messageStore.storeMessage(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MessageResult>() {
                    @Override
                    public void onError(Throwable e) {
                        Logger.d(this, "Store message error");
                    }

                    @Override
                    public void onNext(MessageResult messageResult) {
                        messageView.addMessageToList(messageResult);
                    }

                    @Override
                    public void onCompleted() {
//                        sendMessageUseCase.execute(result)
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(new Subscriber<MessageResult>() {
//                                    @Override
//                                    public void onCompleted() {}
//
//                                    @Override
//                                    public void onError(Throwable e) {}
//
//                                    @Override
//                                    public void onNext(MessageResult messageResult) {
//                                        messageView.updateDeliveryStatus(messageResult.getMessageId(), messageResult.getReceiptId(), messageResult.getMessageStatus());
//                                    }
//                                });
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void sendImageMessage(String toId, String fromId, String fileUri) {
        Message m = new Message();
        ImageMessage imageMessage = new ImageMessage();
        imageMessage.setFileUri(fileUri);
        m.setImageMessage(imageMessage);

        MessageResult result = new MessageResult(toId, fromId, GsonProvider.getGson().toJson(m));
        result.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);
        result.setTime(DateTime.now());

        Subscription subscription = messageStore.storeMessage(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MessageResult>() {
                    @Override
                    public void onError(Throwable e) {
                        Logger.d(this, "Store message error");
                    }

                    @Override
                    public void onNext(MessageResult messageResult) {
                        messageView.addMessageToList(messageResult);

                        File image = saveBitmapToFile(new File(fileUri));
                        Logger.d(this, "File size(MB): "+image.length()/(1024*1024));
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String filename = timeStamp;
                        int i = image.getName().lastIndexOf('.');
                        if (i > 0) {
                            filename = filename + image.getName().substring(i);
                        } else {
                            filename = filename + "." + image.getName();
                        }
                        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), image);
                        MultipartBody.Part imageFileBody = MultipartBody.Part.createFormData("image", filename, requestBody);
                        messageApi.uploadImageData(imageFileBody)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<MessageDataResponse>() {
                                    @Override
                                    public void onCompleted() {}
                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        Logger.d(this, e.getMessage());
                                    }
                                    @Override
                                    public void onNext(MessageDataResponse dataResponse) {
                                        Logger.d(this, "Image uploaded: "+dataResponse);
                                        Message m = new Message();
                                        ImageMessage imageMessage = new ImageMessage();
                                        imageMessage.setImageUrl(dataResponse.getDataUrl());
                                        imageMessage.setFileUri(fileUri);
                                        m.setImageMessage(imageMessage);
                                        result.setMessage(GsonProvider.getGson().toJson(m));
                                        messageStore.updateMessage(result)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io())
                                                .subscribe(new Subscriber<MessageResult>() {
                                                    @Override
                                                    public void onCompleted() {}

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onNext(MessageResult messageResult) {
                                                        sendMessageUseCase.execute(result)
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(new Subscriber<MessageResult>() {
                                                                    @Override
                                                                    public void onCompleted() {}

                                                                    @Override
                                                                    public void onError(Throwable e) {}

                                                                    @Override
                                                                    public void onNext(MessageResult messageResult) {
                                                                        Logger.d("SendMessage: "+messageResult.toString());
                                                                        messageView.updateDeliveryStatus(messageResult.getMessageId(), messageResult.getReceiptId(), messageResult.getMessageStatus());
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                    @Override
                    public void onCompleted() {
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void sendAudioMessage(String toId, String fromId, String audioFileUri) {
        Message m = new Message();
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setFileUri(audioFileUri);
        m.setAudioMessage(audioMessage);

        MessageResult result = new MessageResult(toId, fromId, GsonProvider.getGson().toJson(m));
        result.setMessageStatus(MessageResult.MessageStatus.NOT_SENT);
        result.setTime(DateTime.now());

        Subscription subscription = messageStore.storeMessage(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MessageResult>() {
                    @Override
                    public void onError(Throwable e) {
                        Logger.d(this, "Store message error");
                    }

                    @Override
                    public void onNext(MessageResult messageResult) {
                        messageView.addMessageToList(messageResult);

                        File image = new File(audioFileUri);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String filename = timeStamp;
                        int i = image.getName().lastIndexOf('.');
                        if (i > 0) {
                            filename = filename + image.getName().substring(i);
                        } else {
                            filename = filename + "." + image.getName();
                        }
                        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), image);
                        MultipartBody.Part audioFileBody = MultipartBody.Part.createFormData("audio", filename, requestBody);
                        messageApi.uploadAudioData(audioFileBody)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<MessageDataResponse>() {
                                    @Override
                                    public void onCompleted() {}
                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        Logger.d(this, e.getMessage());
                                    }
                                    @Override
                                    public void onNext(MessageDataResponse dataResponse) {
                                        Logger.d(this, "Image uploaded: "+dataResponse);
                                        Message m = new Message();
                                        AudioMessage audioMessage1 = new AudioMessage();
                                        audioMessage1.setAudioUrl(dataResponse.getDataUrl());
                                        audioMessage1.setFileUri(audioFileUri);
                                        m.setAudioMessage(audioMessage1);
                                        result.setMessage(GsonProvider.getGson().toJson(m));
                                        messageStore.updateMessage(result)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io())
                                                .subscribe(new Subscriber<MessageResult>() {
                                                    @Override
                                                    public void onCompleted() {}

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onNext(MessageResult messageResult) {
                                                        sendMessageUseCase.execute(result)
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(new Subscriber<MessageResult>() {
                                                                    @Override
                                                                    public void onCompleted() {}

                                                                    @Override
                                                                    public void onError(Throwable e) {}

                                                                    @Override
                                                                    public void onNext(MessageResult messageResult) {
                                                                        Logger.d("SendMessage: "+messageResult.toString());
                                                                        messageView.updateDeliveryStatus(messageResult.getMessageId(), messageResult.getReceiptId(), messageResult.getMessageStatus());
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                    @Override
                    public void onCompleted() {
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void sendChatState(String chatId, ChatState chatState) {
        Subscription subscription = messageController.sendChatState(chatId, chatState)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Override
    public void getLastActivity(String chatId) {
        Subscription subscription = messageController.getLastActivity(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {
                        messageView.updateLastActivity(null);
                    }

                    @Override
                    public void onNext(String time) {
                        messageView.updateLastActivity(time);
                    }
                });

        compositeSubscription.add(subscription);
    }

    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void sendReadReceipt(String chatId) {
        // TODO: send only if there is an unsent receipt
        Subscription subscription = sendReadReceiptUseCase.execute(chatId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResult -> {});

        compositeSubscription.add(subscription);
    }

    @Override
    public void attachView(MessageContract.View view) {
        this.messageView = view;
    }

    @Override
    public void detachView() {
        messageView = null;
        compositeSubscription.clear();
    }
}