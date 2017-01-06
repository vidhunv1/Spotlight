package com.stairway.spotlight.screens.home.new_chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stairway.data.config.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.screens.home.new_chat.di.NewChatViewModule;
import com.stairway.spotlight.screens.message.MessageActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * Created by vidhun on 01/09/16.
 */
public class NewChatFragment extends BaseFragment implements NewChatContract.View, NewChatAdapter.ContactClickListener{
    @Bind(R.id.rv_contact_list)
    RecyclerView contactList;

    @Inject
    NewChatPresenter newChatPresenter;

    NewChatAdapter newChatAdapter;

    public NewChatFragment() {
    }

    public static NewChatFragment getInstance() {
        NewChatFragment newChatFragment = new NewChatFragment();
        return newChatFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newChatAdapter = new NewChatAdapter(this, new ArrayList<>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        ButterKnife.bind(this, view);
        OverScrollDecoratorHelper.setUpOverScroll(contactList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        Logger.d(this, " onCreateView");
        contactList.setLayoutManager(new LinearLayoutManager(getActivity()));
        contactList.setAdapter(newChatAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        newChatPresenter.attachView(this);
        newChatPresenter.initContactList();
    }

    @Override
    public void onPause() {
        super.onPause();
        newChatPresenter.detachView();
    }

    @Override
    public void displayContactList(ArrayList<NewChatItemModel> newChatItemModels) {
        Logger.d(this, "Display contact list");
    }

    @Override
    public void addContact(NewChatItemModel newChatItemModel) {
        newChatAdapter.addContact(newChatItemModel);
    }

    @Override
    public void addContacts(List<NewChatItemModel> newChatItemModel) {
        newChatAdapter.addContacts(newChatItemModel);
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new NewChatViewModule()).inject(this);
    }

    @Override
    public void onContactItemClicked(String userId) {
        startActivity(MessageActivity.callingIntent(this.getActivity(), userId));
    }
}
