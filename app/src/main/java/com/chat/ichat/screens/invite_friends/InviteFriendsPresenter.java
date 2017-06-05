package com.chat.ichat.screens.invite_friends;

import com.chat.ichat.core.Logger;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.ContactsContent;
import com.chat.ichat.models.ContactResult;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by vidhun on 03/06/17.
 */

public class InviteFriendsPresenter implements InviteFriendsContract.Presenter{
    private InviteFriendsContract.View inviteFriendsView;
    private CompositeSubscription compositeSubscription;
    private ContactsContent contactsContent;
    private ContactStore contactStore;

    public InviteFriendsPresenter(ContactsContent contactsContent, ContactStore contactStore) {
        this.contactsContent = contactsContent;
        this.contactStore = contactStore;
    }

    @Override
    public void attachView(InviteFriendsContract.View view) {
        this.inviteFriendsView = view;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void detachView() {
        compositeSubscription.clear();
        this.inviteFriendsView = null;
    }

    @Override
    public void getInviteList() {
        contactsContent.getContacts()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ContactResult>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<ContactResult> cr) {
                        Logger.d(this, "Phone Contacts: "+cr.size());
                        contactStore.getContacts()
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<List<ContactResult>>() {
                                    @Override
                                    public void onCompleted() {}

                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onNext(List<ContactResult> contactResults) {
                                        for (ContactResult phoneContact : cr) {
                                            for (ContactResult cont : contactResults) {
                                                if(phoneContact.getPhoneNumber().equals(cont.getPhoneNumber())) {
                                                    cr.remove(phoneContact);
                                                }
                                            }
                                        }
                                        Logger.d(this, "Invite friends: "+cr.size());
                                        inviteFriendsView.displayInviteList(cr);
                                    }
                                });
                    }
                });

    }
}
