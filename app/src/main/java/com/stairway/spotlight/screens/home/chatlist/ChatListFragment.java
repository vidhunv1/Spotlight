package com.stairway.spotlight.screens.home.chatlist;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stairway.data.manager.Logger;
import com.stairway.data.model.ChatListItem;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.screens.chat.ChatActivity;
import com.stairway.spotlight.screens.home.chatlist.di.ChatListViewModule;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatListFragment extends BaseFragment implements ChatListContract.View, ChatListAdapter.ChatClickListener{

    @Bind(R.id.chat_list)
    RecyclerView chatList;

    @Inject
    ChatListPresenter presenter;

    public ChatListFragment() {
    }

    public static ChatListFragment newInstance() {
        ChatListFragment fragment = new ChatListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        ButterKnife.bind(this, view);

        Logger.v("[ChatListFragment] onCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize recycler view
        chatList.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.v("[ChatListFragment] onResume");
        presenter.attachView(this);
        presenter.initChatList();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.detachView();
    }

    @Override
    public void onChatItemClicked(long userId) {
        startActivity(ChatActivity.callingIntent(this.getActivity()));

    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.getAppComponent().plus(new ChatListViewModule()).inject(this);
    }

    @Override
    public void displayChatList(ArrayList<ChatListItem> chats) {

        chatList.setAdapter(new ChatListAdapter(getActivity(), chats, this));
    }

    @Override
    public void setDeliveryStatus(int status, int chatId) {
    }
}
