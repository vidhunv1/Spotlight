package com.chat.ichat.config;

/**
 * Created by vidhun on 06/04/17.
 */

public class AnalyticsContants {

    public static class Event {
        public static final String ADD_CONTACT_POPUP = "add_contact_popup";
        public static final String ADD_CONTACT_SUCCESS = "add_contact_success";
        public static final String ADD_CONTACT_FAILURE = "add_contact_failure";
        public static final String MAIN_ADD_CONTACT = "main_add_contact";
        public static final String OTHER_ADD_CONTACT = "other_add_contact";
        public static final String SEARCH = "search";
        public static final String SEND_MESSAGE = "send_message";
        public static final String SELECT_CHAT = "select_chat";
        public static final String SELECT_CONTACT = "select_contact";
        public static final String OPEN_CHAT = "open_chat";
        public static final String SELECT_MESSAGE = "select_message";
        public static final String BLOCK_USER = "block_user";
        public static final String UNBLOCK_USER = "unblock_user";
        public static final String DELETE_USER = "delete_user";
        public static final String DELETE_MESSAGE = "delete_message";
        public static final String DELETE_CHAT = "delete_chat";
        public static final String CLEAR_HISTORY_CHAT = "clear_history_chat";
        public static final String ADD_SHORTCUT = "add_shortcut";
        public static final String MESSAGE_SMILEY = "message_smiley";
        public static final String PROFILE_MESSAGE_USER = "profile_message_user";
        public static final String MESSAGE_CAMERA = "message_camera";
        public static final String MESSAGE_INFO = "message_info";
        public static final String LOGOUT = "logout";
        public static final String EDIT_NAME = "edit_name";
        public static final String OPEN_DRAWER_MENU = "open_drawer_menu";
        public static final String RECEIVE_MESSAGE = "receive_message";
        public static final String RECEIVE_MESSAGE_STATUS = "receive_message_status";
        public static final String RECEIVE_PRESENCE = "receive_presence";
        public static final String RECEIVE_CHAT_STATE = "receive_chat_state";
        public static final String CLICK_BOT_MENU = "click_bot_menu";
        public static final String KEYBOARD_TYPE = "keyboard_type";
        public static final String SMILEY_SELECTED = "smiley_selected";
        public static final String EXCEPTION_XMPP_CONNECTION = "xmpp_connection_failed";

        protected Event() {
        }
    }

    public static class Param {
        public static final String OTHER_USER_ID = "other_user_id";
        public static final String OTHER_USER_NAME = "other_user_name";
        public  static final String MESSAGE = "message";
        public static final String KEYBOARD_TYPE = "keyboard_type";
        public static final String EXCEPTION_STACK_TRACE = "exception_stack_trace";

        protected Param() {
        }
    }
}
