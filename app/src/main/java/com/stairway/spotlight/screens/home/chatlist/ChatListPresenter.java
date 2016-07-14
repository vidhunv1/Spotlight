package com.stairway.spotlight.screens.home.chatlist;

import com.stairway.data.manager.Logger;
import com.stairway.data.model.ChatListItem;

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

    }

    @Override
    public void initChatList() {
        Logger.v("[ChatListPresenter] initChatList");
        ArrayList<ChatListItem> chatList = new ArrayList<>();

        // TODO: Create and call database local storage for messages
        chatList.add(new ChatListItem(
                1,
                "Vidhun Vinod",
                "I realized that design skills are a new kind of literacy. The whole reason why I had suffered the consequences of poor handwriting was because handwriting was a part of communication. The only reason that I could write anything at all was that I was literate.",
                "YESTERDAY",5));
        chatList.add(new ChatListItem(2, "Ankit", "The whole reason why I had suffered the consequences of poor handwriting was because handwriting was a part of communication.", "2:30 PM", 1));
        chatList.add(new ChatListItem(3, "Arjun", "The only reason that I could write anything at all was that I was literate.", "YESTERDAY",10));
        chatList.add(new ChatListItem(4, "JarJarBinks", "hello", "YESTERDAY",3));
        chatList.add(new ChatListItem(5, "SnapChat", "snap", "YESTERDAY",1));
        chatList.add(new ChatListItem(6, "Youtube", "video", "YESTERDAY",1));
        chatList.add(new ChatListItem(7, "HBO", "hello", "YESTERDAY",1));
        chatList.add(new ChatListItem(8, "Jalasas", "hello!!", "WEDNESDAY",1));
        chatList.add(new ChatListItem(9, "Vidhun Vinod", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItem(10, "Jalasas", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItem(11, "Vidhun Vinod", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItem(12, "Jalasas", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItem(13, "Vidhun Vinod", "hi", "WEDNESDAY",1));
        chatList.add(new ChatListItem(14, "Jalasas", "hi", "WEDNESDAY",1));

        view.displayChatList(chatList);
    }
}
