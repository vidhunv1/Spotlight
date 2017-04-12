//package com.stairway.spotlight.screens.home.chats;
//
//import android.os.Bundle;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.stairway.data.config.Logger;
//import com.stairway.data.source.message.MessageResult;
//import com.stairway.spotlight.R;
//import com.stairway.spotlight.core.di.component.ComponentContainer;
//import com.stairway.spotlight.core.BaseFragment;
//import com.stairway.spotlight.screens.home.ChatListContract;
//import com.stairway.spotlight.screens.message.MessageActivity;
//import com.stairway.spotlight.screens.home.di.ChatListViewModule;
//
//import org.jivesoftware.smackx.chatstates.ChatState;
//
//import java.util.List;
//
//import javax.inject.Inject;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
//
//public class ChatListFragment extends BaseFragment implements ChatListContract.View, ChatListAdapter.ChatClickListener{
//
//    @Bind(R.id.rv_chat_list)
//    RecyclerView chatList;
//
//    @Inject
//    ChatListPresenter presenter;
//
//    private ChatListAdapter chatListAdapter;
//    public ChatListFragment() {
//    }
//
//    public static ChatListFragment getInstance() {
//        ChatListFragment fragment = new ChatListFragment();
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
//        ButterKnife.bind(this, view);
//        OverScrollDecoratorHelper.setUpOverScroll(chatList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
//
//        return view;
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        // Initialize recycler view
//        chatList.setLayoutManager(new LinearLayoutManager(getActivity()));
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Logger.v(this, " onResume");
//        presenter.attachView(this);
//        presenter.initChatList();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        presenter.detachView();
//    }
//
//    @Override
//    public void onChatItemClicked(String userName) {
//        startActivity(MessageActivity.callingIntent(this.getActivity(), userName));
//    }
//
//    @Override
//    protected void injectComponent(ComponentContainer componentContainer) {
//        componentContainer.userSessionComponent().plus(new ChatListViewModule()).inject(this);
//    }
//
//    @Override
//    public void displayChatList(List<ChatListItemModel> chats) {
//        chatListAdapter = new ChatListAdapter(getActivity(), chats, this);
//        chatList.setAdapter(chatListAdapter);
//    }
//
//    @Override
//    public void setDeliveryStatus(int status, int chatId) {
//    }
//
//    @Override
//    public void addNewMessage(MessageResult messageResult) {
//        ChatListItemModel item = new ChatListItemModel(messageResult.getChatId(), messageResult.getChatId(), messageResult.getMessage(), messageResult.getTime(),1);
//        chatListAdapter.newChatMessage(item);
//        Logger.d(this, "new notification: "+item);
//    }
//
//    @Override
//    public void showChatState(String from, ChatState chatState) {
//        if(chatState == ChatState.composing)
//            chatListAdapter.setChatState(from, "Typing...");
//        else
//            chatListAdapter.resetChatState(from);
//    }
//}