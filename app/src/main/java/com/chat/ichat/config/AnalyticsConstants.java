package com.chat.ichat.config;

/**
 * Created by vidhun on 06/04/17.
 */

public class AnalyticsConstants {

    public static class Event {
        /* Permissions */
        public static final String PERMISSION_READ_CONTACT_SHOW = "permission_readContact_show";
        public static final String PERMISSION_READ_CONTACT_ALLOW = "permission_readContact_allow";
        public static final String PERMISSION_READ_CONTACT_DENY = "permission_readContact_deny";

        public static final String PERMISSION_LOCATION_SHOW = "permission_location_show";
        public static final String PERMISSION_LOCATION_ALLOW = "permission_location_allow";
        public static final String PERMISSION_LOCATION_DENY = "permission_location_deny";

        public static final String PERMISSION_STORAGE_SHOW = "permission_storage_show";
        public static final String PERMISSION_STORAGE_ALLOW = "permission_storage_allow";
        public static final String PERMISSION_STORAGE_DENY = "permission_storage_deny";

        public static final String PERMISSION_SEND_SMS_SHOW = "permission_sendSms_show";
        public static final String PERMISSION_SEND_SMS_ALLOW = "permission_sendSms_allow";
        public static final String PERMISSION_SEND_SMS_DENY = "permission_sendSms_deny";

        public static final String PERMISSION_RECORD_AUDIO_SHOW = "permission_recordAudio_show";
        public static final String PERMISSION_RECORD_AUDIO_ALLOW = "permission_recordAudio_allow";
        public static final String PERMISSION_RECORD_AUDIO_DENY = "permission_recordAudio_deny";

        /* Welcome Activity */
        public static final String WELCOME_SCREEN = "welcome";
        public static final String WELCOME_PAGE_1 = "welcome_page_1";
        public static final String WELCOME_PAGE_2 = "welcome_page_2";
        public static final String WELCOME_PAGE_3 = "welcome_page_3";
        public static final String WELCOME_CLICK_START_MESSAGING = "login_click_startMessaging";

        /* Signup Activity */
        public static final String SIGNUP_SCREEN = "signup";
        public static final String SIGNUP_DONE = "signup_done";
        public static final String SIGNUP_ERROR_COUNTRYCODE = "signup_error_countryCode";
        public static final String SIGNUP_ERROR_PHONE = "signup_error_phone";
        public static final String SIGNUP_SUCCESS = "login_success";

        /* Phone Verify Activity */
        public static final String PHONE_VERIFY_SCREEN = "phoneVerify";
        public static final String PHONE_VERIFY_DONE = "phoneVerify_done";
        public static final String PHONE_VERIFY_ERROR_OTP = "phoneVerify_error_otp";
        public static final String PHONE_VERIFY_SUCCESS = "phoneVerify_success";

        /* Home Activity */
        public static final String HOME_SCREEN = "home";
        public static final String HOME_CLICK_FAB = "home_click_fab";
        public static final String HOME_CHAT_OPEN = "home_chat_open";
        public static final String HOME_CLICK_DISCOVER_BOTS = "home_click_discoverBots";
        public static final String HOME_CLICK_SETTINGS = "home_click_settings";
        public static final String HOME_CHAT_CLEAR = "home_chat_clear";
        public static final String HOME_CHAT_DELETE = "home_chat_delete";
        public static final String HOME_TAB_FAVORITES = "home_tab_favorites";
        public static final String HOME_TAB_CHAT = "home_tab_chat";
        public static final String HOME_TAB_DISCOVER = "home_tab_discover";

        /* Contacts / NewChat */
        public static final String CONTACTS_CLICK_SEARCH = "%s_click_search";
        public static final String CONTACTS_BACK = "%s_back";
        public static final String CONTACTS_INVITE_FRIENDS = "%s_inviteFriends";
        public static final String CONTACTS_CHAT_OPEN = "%s_chat_open";
        public static final String CONTACTS_SEARCH_CHAT_OPEN = "%s_search_chat_open";
        public static final String CONTACTS_SUGGESTED_CHAT_OPEN = "%s_suggested_chat_open";
        public static final String CONTACTS_CLICK_INVITECONTACT = "%s_click_inviteContact";
        public static final String CONTACTS_CLICK_DISCOVERBOTS = "%s_click_discoverBots";
        public static final String CONTACTS_CLICK_PEOPLENEARBY = "%s_click_peopleNearby";
        public static final String CONTACTS_SEARCH = "%s_search";

        /* DiscoverBots */
        public static final String DISCOVER_BOTS_SCREEN = "discoverBots_screen";
        public static final String DISCOVER_BOTS_BACK = "discoverBots_back";
        public static final String DISCOVER_BOTS_CHAT_OPEN = "discoverBots_chat_open";

//        /* PeopleNearby */
//        public static final String PEOPLE_NEARBY_SCREEN = "peopleNearby_screen";
//        public static final String PEOPLE_NEARBY_BACK = "peopleNearby_back";
//        public static final String PEOPLE_NEARBY_CHAT_OPEN = "peopleNearby_chat_open";
//        public static final String PEOPLE_NEARBY_CLEAR_LOCATION = "peopleNearby_clear_location";

        /* Settings */
        public static final String SETTINGS_SCREEN = "settings_screen";
        public static final String SETTINGS_BACK = "settings_back";

        /* InviteFriends */
        public static final String INVITE_FRIENDS_SCREEN = "inviteFriends_screen";
        public static final String INVITE_FRIENDS_BACK = "inviteFriends_back";
        public static final String INVITE_FRIENDS_CHECK_CONTACT = "inviteFriends_check_contact";
        public static final String INVITE_FRIENDS_UNCHECK_CONTACT = "inviteFriends_uncheck_contact";
        public static final String INVITE_FRIENDS_CHECK_SELECTALL = "inviteFriends_check_selectAll";
        public static final String INVITE_FRIENDS_UNCHECK_SELECTALL = "inviteFriends_uncheck_selectAll";
        public static final String INVITE_FRIENDS_CLICK_INVITE = "inviteFriends_click_invite";
        public static final String INVITE_FRIENDS_ONINVITESEND = "inviteFriends_onInviteSend";

        /* Search */
        public static final String SEARCH_SCREEN = "search_screen";
        public static final String SEARCH_BACK = "search_back";
        public static final String SEARCH_QUERY = "search_query";
        public static final String SEARCH_CLEAR = "search_clear";
        public static final String SEARCH_CONTACT_CHAT_OPEN = "search_contact_chat_open";
        public static final String SEARCH_USERNAME_CHAT_OPEN = "search_username_chat_open";
        public static final String SEARCH_SUGGESTED_CHAT_OPEN = "search_suggested_chat_open";

        /* Message */
        public static final String MESSAGE_SCREEN = "message_screen";
        public static final String MESSAGE_BACK = "message_back";
        public static final String MESSAGE_CLICK_DP = "message_click_dp";
        public static final String MESSAGE_CLICK_TEXTBOX = "message_click_textBox";
        public static final String MESSAGE_CLICK_SMILEYS = "message_click_smileys";
        public static final String MESSAGE_CLICK_GALLERY = "message_click_gallery";
        public static final String MESSAGE_CLICK_CAMERA = "message_click_camera";
        public static final String MESSAGE_CLICK_GIF = "message_click_gif";
        public static final String MESSAGE_CLICK_AUDIO = "message_click_audio";
        public static final String MESSAGE_CLICK_LOCATION = "message_click_location";
        public static final String MESSAGE_CLICK_SEND = "message_click_send";
        public static final String MESSAGE_CLICK_FAVORITE = "message_click_favorite";
        public static final String MESSAGE_CLICK_PERSISTENT_MENU = "message_click_persistentMenu";
        public static final String MESSAGE_PERSISTENT_MENU_CLICK_ITEM = "message_persistentMenu_click_msg";
        public static final String MESSAGE_QUICK_REPLY_CLICK_MESSAGE = "message_quickReply_click_message";
        public static final String MESSAGE_QUICK_REPLY_CLICK_LOCATION = "message_quickReply_click_loc";
        public static final String MESSAGE_CLICK_BLOCK_CONTACT = "message_block_contact";
        public static final String MESSAGE_LONG_CLICK_MSG = "message_long_click_msg";
        public static final String MESSAGE_CLICK_MSG = "message_click_msg";
        public static final String MESSAGE_AUDIOMSG_PLAY = "message_audio_msg_play";
        public static final String MESSAGE_AUDIOMSG_PAUSE = "message_audio_msg_pause";
        public static final String MESSAGE_SMILEYS_CLICK_SMILEY = "message_smiley_click_smiley";
        public static final String MESSAGE_SMILEYS_CLICK_CATEGORY = "message_smiley_click_category_%d";
        public static final String MESSAGE_SMILEYS_CLICK_BACKSPACE = "message_smiley_click_backspace";
        public static final String MESSAGE_GALLERY_CLICK_IMAGE = "message_gallery_click_image";
        public static final String MESSAGE_OPEN_GALLERY = "message_open_gallery";
        public static final String MESSAGE_GIF_CLICK_SEARCH = "message_gif_click_search";
        public static final String MESSAGE_GIF_CLICK_TRENDING = "message_gif_click_trending";
        public static final String MESSAGE_GIF_CLICK_CATEGORY = "message_gif_click_category";
        public static final String MESSAGE_GIF_CLICK_GIF = "message_gif_click_gif";
        public static final String MESSAGE_GIF_CATEGORY_SELECT = "message_gif_category_select";
        public static final String MESSAGE_GIF_CLICK_SEND = "message_gif_click_gif";
        public static final String MESSAGE_GIF_CLICK_BACK = "message_gif_click_back";
        public static final String MESSAGE_GIF_SEARCH_QUERY = "message_gif_search_query";
        public static final String MESSAGE_AUDIO_START_RECORD = "message_audio_start_record";
        public static final String MESSAGE_AUDIO_STOP_RECORD = "message_audio_stop_record";
        public static final String MESSAGE_AUDIO_CANCEL_RECORD = "message_audio_cancel_record";
        public static final String MESSAGE_UNKNOWN_REPORT_SPAM = "message_unknown_report_spam";
        public static final String MESSAGE_UNKNOWN_BLOCK = "message_unknown_block";


        /* UserProfile */
        public static final String USER_PROFILE_SCREEN = "userProfile_screen";
        public static final String USER_PROFILE_BACK = "userProfile_back";
        public static final String USER_PROFILE_BLOCK = "userProfile_block";
        public static final String USER_PROFILE_DELETE = "userProfile_delete";
        public static final String USER_PROFILE_ADD_SHORTCUT = "userProfile_click_addShortcut";

        /* WebView */
        public static final String WEBVIEW_SCREEN = "userProfile_screen";
        public static final String WEBVIEW_BACK = "userProfile_back";
        public static final String WEBVIEW_CLOSE = "userProfile_close";

        /* Popups */
        public static final String POPUP_ADD_CONTACT_DISMISS = "popup_addContact_dismiss";
        public static final String POPUP_ADD_CONTACT_ADD = "popup_addContact_add";
        public static final String POPUP_ADD_CONTACT_ERROR_DISMISS = "popup_addContact_error_dismiss";
        public static final String POPUP_ADD_CONTACT_ERROR_OK = "popup_addContact_error_ok";
        public static final String POPUP_ADD_CONTACT_ERROR_SHOW = "popup_addContact_error_show";
        public static final String POPUP_ADD_CONTACT_SUCCESS_DISMISS = "popup_addContact_success_dismiss";
        public static final String POPUP_ADD_CONTACT_SUCCESS_OK = "popup_addContact_success_ok";
        public static final String POPUP_ADD_CONTACT_SUCCESS_SHOW = "popup_addContact_success_show";

        public static final String POPUP_EDIT_DP_FROM_CAMERA = "popup_editDp_fromCamera";
        public static final String POPUP_EDIT_DP_FROM_GALLERY = "popup_editDp_fromGallery";
        public static final String POPUP_EDIT_DP_DELETE_PHOTO = "popup_editDp_deletePhoto";
        public static final String POPUP_EDIT_DP_DISMISS = "popup_editDP_dismiss";

        public static final String POPUP_MESSAGE_ACTION_DISMISS = "popup_message_action_dismiss";
        public static final String POPUP_MESSAGE_ACTION_COPY = "popup_message_action_copy";
        public static final String POPUP_MESSAGE_ACTION_DETAILS = "popup_message_action_details";
        public static final String POPUP_MESSAGE_ACTION_DELETE = "popup_message_action_delete";

        public static final String RECEIVE_MESSAGE = "receive_message";
        protected Event() {}
    }

    public static class Param {
        public static final String RECIPIENT_USER_ID = "recipient_user_id";
        public static final String RECIPIENT_USER_NAME = "recipient_user_name";
        public static final String RECIPIENT_NAME = "recipient_name";
        public static final String MESSAGE = "message";

        public static final String TEXT = "text";
        public static final String COUNT = "count";
        public static final String ERROR_NAME = "error_name";

        protected Param() {}
    }
}
