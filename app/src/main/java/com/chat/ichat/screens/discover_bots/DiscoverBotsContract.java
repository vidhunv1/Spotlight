package com.chat.ichat.screens.discover_bots;

import com.chat.ichat.api.bot.DiscoverBotsResponse;
import com.chat.ichat.core.BasePresenter;
import com.chat.ichat.core.BaseView;

/**
 * Created by vidhun on 01/06/17.
 */
public interface DiscoverBotsContract {
    interface View extends BaseView {
        void displayBots(DiscoverBotsResponse discoverBotsResponse);
        void navigateToMessage(String username, String coverPicture, String description, String botCategory);
    }
    interface Presenter extends BasePresenter<View> {
        void discoverBots();
        void openContact(String userId, String coverPicture, String description, String botCategory);
    }
}