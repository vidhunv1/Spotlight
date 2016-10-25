package com.stairway.spotlight.screens.home.chats;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stairway.data.manager.Logger;
import com.stairway.spotlight.R;
import com.stairway.spotlight.core.di.component.ComponentContainer;
import com.stairway.spotlight.core.BaseFragment;
import com.stairway.spotlight.screens.message.MessageActivity;
import com.stairway.spotlight.screens.home.chats.di.ChatListViewModule;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatListFragment extends BaseFragment implements ChatListContract.View, ChatListAdapter.ChatClickListener{

    @Bind(R.id.rv_chat_list)
    RecyclerView chatList;

    @Inject
    ChatListPresenter presenter;

    public ChatListFragment() {
    }

    public static ChatListFragment getInstance() {
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
    public void onChatItemClicked(String userId) {
        startActivity(MessageActivity.callingIntent(this.getActivity(), userId));
    }

    @Override
    protected void injectComponent(ComponentContainer componentContainer) {
        componentContainer.userSessionComponent().plus(new ChatListViewModule()).inject(this);
    }

    @Override
    public void displayChatList(List<ChatListItemModel> chats) {
        chatList.setAdapter(new ChatListAdapter(getActivity(), chats, this));
    }

    @Override
    public void setDeliveryStatus(int status, int chatId) {
    }
}