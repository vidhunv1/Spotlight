package com.chat.ichat.screens.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chat.ichat.MessageController;
import com.chat.ichat.R;
import com.chat.ichat.UserSessionManager;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseFragment;
import com.chat.ichat.core.Logger;
import com.chat.ichat.core.NotificationController;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.MessageStore;
import com.chat.ichat.models.ContactResult;
import com.chat.ichat.models.MessageResult;
import com.chat.ichat.models.UserSession;
import com.chat.ichat.screens.message.MessageActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatListFragment extends BaseFragment implements HomeContract.View, ChatListAdapter.ChatClickListener{

    @Bind(R.id.rv_chat_list)
    RecyclerView chatList;

    HomePresenter presenter;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    private List<ChatItem> chats;
    private ChatListAdapter chatListAdapter;

    private UserSession userSession;

    private SharedPreferences sharedPreferences;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseAnalytics firebaseAnalytics;
    public ChatListFragment() {
    }

    public static ChatListFragment getInstance() {
        ChatListFragment fragment = new ChatListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new HomePresenter(MessageController.getInstance(), ApiManager.getAppApi(), MessageStore.getInstance(), ContactStore.getInstance(), ApiManager.getUserApi(), BotDetailsStore.getInstance(), ApiManager.getBotApi());
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize recycler view
        chatList.setLayoutManager(new LinearLayoutManager(getActivity()));
        layoutManager = new LinearLayoutManager(getActivity());
        chatList.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator animator = chatList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator)
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        chatListAdapter = new ChatListAdapter(getActivity(), this);
        chatList.setAdapter(chatListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        userSession = UserSessionManager.getInstance().load();
        presenter.loadChatList();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.detachView();
    }

    @Override
    public void showError(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void setDeliveryStatus(int status, int chatId) {

    }

    @Override
    public void displayChatList(List<ChatItem> chatList) {
        Logger.d(this, "DisplayChatList: "+chatList.get(0).toString());
        this.chats = chatList;
        chatListAdapter.setChatList(chats);
    }

    @Override
    public void showChatState(String from, ChatState chatState) {
        if(chatState == ChatState.composing || chatState == ChatState.active) {
            chatListAdapter.setChatState(from, "Typing...");
            final Handler handler = new Handler();
            handler.postDelayed(() -> chatListAdapter.resetChatState(from), 10000);
        } else if(chatState == ChatState.gone || chatState == ChatState.inactive) {
            Logger.d(this, "ChatState: "+chatState.name());
            chatListAdapter.resetChatState(from);
        }
    }

    @Override
    public void showUpdate(int versionCode, String versionName, boolean isMandatory) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Update Available");
        alertDialog.setMessage("New version contains improvements and bug fixes.\nPlease update to version "+versionName+".");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "UPDATE", (dialog, which) -> {
            final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
            dialog.dismiss();
        });
        alertDialog.show();
    }

    @Override
    public void removeChatItem(String chatId) {
        chatListAdapter.removeChat(chatId);
    }

    @Override
    public void showContactAddedSuccess(String contactName, String username, boolean isExistingContact) {
        AndroidUtils.hideSoftInput(getActivity());
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_SUCCESS_SHOW, null);

        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }

        String message;
        if(isExistingContact) {
            message = "<b>" + contactName + "</b> is already in your contacts on iChat.";
        } else {
            message = "<b>" + contactName + "</b> is added to your contacts on iChat.";
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(Html.fromHtml(message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_SUCCESS_OK, null);
            dialog.dismiss();
        });
        alertDialog.setOnDismissListener(dialog -> {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_SUCCESS_DISMISS, null);
        });
        alertDialog.show();

        presenter.loadChatList();
    }

    @Override
    public void showInvalidIDError() {
        AndroidUtils.hideSoftInput(getActivity());

        firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_ERROR_SHOW, null);

        if(progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage("Please enter a valid iChat ID.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_ERROR_OK, null);
            dialog.dismiss();
        });
        alertDialog.setOnDismissListener(dialog -> firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_ERROR_DISMISS, null));
        alertDialog.show();
    }

    @Override
    public void onSyncSuccess() {

    }

    @Override
    public void onChatItemClicked(String username) {
		/*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, username);
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_CHAT_OPEN, bundle);

        startActivity(MessageActivity.callingIntent(getActivity(), username));
    }

    @Override
    public void onChatItemLongClicked(String username) {
        BottomSheetDialog chatActionsDialog = new BottomSheetDialog(getActivity());
        View chatActionsView = getActivity().getLayoutInflater().inflate(R.layout.bottomsheet_chat_actions, null);
        chatActionsDialog.setContentView(chatActionsView);
        chatActionsDialog.show();

        LinearLayout llClearHistory = (LinearLayout)chatActionsView.findViewById(R.id.clear_history);
        llClearHistory.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, username);
            presenter.deleteChat(username);
            chatActionsDialog.dismiss();
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_CHAT_CLEAR, bundle);
        });

        LinearLayout llDelteChat = (LinearLayout)chatActionsView.findViewById(R.id.delete_chat);
        llDelteChat.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, username);
            presenter.deleteChat(username);
            chatActionsDialog.dismiss();
            firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_CHAT_DELETE, bundle);
        });

		/*              Analytics           */
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_NAME, username);
        firebaseAnalytics.logEvent(AnalyticsConstants.Event.HOME_CHAT_OPEN, bundle);
    }

    @Override
    public void onMessageReceived(MessageResult messageResult, ContactResult from) {
        Logger.d(this, "MessageReceived");
        messageResult.setName(from.getContactName());
        ChatItem item = new ChatItem(messageResult.getChatId(), messageResult.getName(), messageResult.getMessage(), messageResult.getTime(), messageResult.getMessageStatus(), messageResult.getReceiptId(), messageResult.getMessageId(), 1);
        item.setProfileDP(from.getProfileDP());
        this.chats = chatListAdapter.newChatMessage(item);
        chatList.scrollToPosition(0);
    }

    @Override
    public void onMessageStatusReceived(String messageId, String chatId, String deliveryReceiptId, MessageResult.MessageStatus messageStatus) {
        super.onMessageStatusReceived(messageId, chatId, deliveryReceiptId, messageStatus);
        Logger.d(this, "deliveryStatus: "+messageStatus.name());
        chatListAdapter.updateDeliveryStatus(messageId, deliveryReceiptId, messageStatus);
    }
}