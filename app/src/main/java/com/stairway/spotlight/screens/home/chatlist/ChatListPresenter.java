package com.stairway.spotlight.screens.home.chatlist;

import com.stairway.data.manager.Logger;

import java.util.ArrayList;

/**
 * Created by vidhun on 13/07/16.
 */
public class ChatListPresenter implements ChatListContract.Presenter {
    private ChatListContract.View view;

    public ChatListPresenter() {
    }

    @Override
    public void attachView(ChatListContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void initChatList() {
        Logger.v("[ChatListPresenter] initChatList");
        ArrayList<ChatListItemModel> chatList = new ArrayList<>();

        // TODO: Create and call database local storage for messages
        chatList.add(new ChatListItemModel(
                1,
                "Vidhun Vinod",
                "I realized that design skills are a new kind of literacy. The whole reason why I had suffered the consequences of poor handwriting was because handwriting was a part of communication. The only reason that I could write anything at all was that I was literate.",
                "YESTERDAY",5));
        chatList.add(new ChatListItemModel(2, "Ankit", "The whole reason why I had suffered the consequences of poor handwriting was because handwriting was a part of communication.", "2:30 PM", 1));
        chatList.add(new ChatListItemModel(3, "Arjun", "The only reason that I could write anything at all was that I was literate.", "YESTERDAY",10));
        chatList.add(new ChatListItemModel(4, "JarJarBinks", "hello", "YESTERDAY",3));
        chatList.add(new ChatListItemModel(5, "SnapChat", "snap", "YESTERDAY",1));
        chatList.add(new ChatListItemModel(6, "Youtube", "video", "YESTERDAY",1));
        chatList.add(new ChatListItemModel(7, "HBO", "hello", "YESTERDAY",1));
        chatList.add(new ChatListItemModel(8, "Jalasas", "hello!!", "WEDNESDAY",1));
        chatList.add(new ChatListItemModel(9, "Vidhun Vinod", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItemModel(10, "Jalasas", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItemModel(11, "Vidhun Vinod", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItemModel(12, "Jalasas", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItemModel(13, "Vidhun Vinod", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItemModel(14, "Jalasas", "hi", "WEDNESDAY",1));

        view.displayChatList(chatList);
    }
}
