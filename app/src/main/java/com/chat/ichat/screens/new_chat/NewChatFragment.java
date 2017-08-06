package com.chat.ichat.screens.new_chat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chat.ichat.R;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.config.AnalyticsConstants;
import com.chat.ichat.core.BaseFragment;
import com.chat.ichat.core.lib.AndroidUtils;
import com.chat.ichat.db.BotDetailsStore;
import com.chat.ichat.db.ContactStore;
import com.chat.ichat.db.ContactsContent;
import com.chat.ichat.screens.invite_friends.InviteFriendsActivity;
import com.chat.ichat.screens.message.MessageActivity;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vidhun on 26/07/17.
 */

public class NewChatFragment extends BaseFragment implements NewChatContract.View, NewChatAdapter.ContactClickListener {
    @Bind(R.id.rv_chat_list)
    RecyclerView contactList;
    NewChatAdapter newChatAdapter;

    private String SCREEN_NAME;

    final ProgressDialog[] progressDialog = new ProgressDialog[1];

    NewChatPresenter newChatPresenter;

    private FirebaseAnalytics firebaseAnalytics;
    private boolean isNewChat;
    LinearLayoutManager linearLayoutManager;

    public static NewChatFragment getInstance() {
        NewChatFragment fragment = new NewChatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newChatPresenter = new NewChatPresenter(ContactStore.getInstance(), new ContactsContent(getActivity()), ApiManager.getUserApi(), BotDetailsStore.getInstance(), ApiManager.getBotApi());
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        this.SCREEN_NAME = "favorites";
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
        isNewChat = true;
        linearLayoutManager = new LinearLayoutManager(getActivity());
        contactList.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemAnimator animator = contactList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator)
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        newChatAdapter = new NewChatAdapter(getActivity(), this);
        contactList.setAdapter(newChatAdapter);

        ContactsContent contactsContent = new ContactsContent(getActivity());
        newChatPresenter = new NewChatPresenter(ContactStore.getInstance(), contactsContent, ApiManager.getUserApi(), BotDetailsStore.getInstance(), ApiManager.getBotApi());

        newChatPresenter.attachView(this);
        newChatPresenter.initContactList(!isNewChat);

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        newChatPresenter.initContactList(!isNewChat);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void showError(String title, String message) {
        if(progressDialog[0]!=null && progressDialog[0].isShowing()) {
            progressDialog[0].dismiss();
        }

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage("\n"+message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void displayContacts(List<NewChatItemModel> newChatItemModel) {
        newChatAdapter.setContactList(newChatItemModel);
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
        newChatPresenter.initContactList(!isNewChat);
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
        alertDialog.show();

        alertDialog.setOnDismissListener(dialog -> firebaseAnalytics.logEvent(AnalyticsConstants.Event.POPUP_ADD_CONTACT_ERROR_DISMISS, null));
    }

    @Override
    public void onContactItemClicked(String userId, int from) {
        startActivity(MessageActivity.callingIntent(getActivity(), userId));
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.RECIPIENT_USER_ID, userId);

        if(from == 0) {
            firebaseAnalytics.logEvent(String.format(AnalyticsConstants.Event.CONTACTS_CHAT_OPEN, SCREEN_NAME), bundle);
        } else if (from == 1) {
            firebaseAnalytics.logEvent(String.format(AnalyticsConstants.Event.CONTACTS_SEARCH_CHAT_OPEN, SCREEN_NAME), bundle);
        }  else if (from == 2) {
            firebaseAnalytics.logEvent(String.format(AnalyticsConstants.Event.CONTACTS_SUGGESTED_CHAT_OPEN, SCREEN_NAME), bundle);
        }
    }

    @Override
    public void onInviteFriendsClicked() {
        startActivity(InviteFriendsActivity.callingIntent(getActivity()));
        firebaseAnalytics.logEvent(String.format(AnalyticsConstants.Event.CONTACTS_INVITE_FRIENDS, SCREEN_NAME), null);
    }

    @Override
    public void onDiscoverBotsClicked() {
        firebaseAnalytics.logEvent(String.format(AnalyticsConstants.Event.CONTACTS_CLICK_DISCOVERBOTS, SCREEN_NAME), null);
    }

    @Override
    public void onInviteContact(String contactName, String number) {
        firebaseAnalytics.logEvent(String.format(AnalyticsConstants.Event.CONTACTS_CLICK_INVITECONTACT, SCREEN_NAME), null);
    }
}
