package com.stairway.spotlight.core.lib;

/**
 * Created by vidhun on 06/01/17.
 */

public class MessageXML {
    public static final String TAG_HEAD = "message";
    public static final String TAG_HEAD_SHORT = "m";

    public static final String TAG_TEXT = "text";

    public static final String TAG_VIDEO = "video";
    public static final String ATTRIBUTE_VIDEO_URL = "url"; // required

    public static final String TAG_IMAGE = "image";
    public static final String ATTRIBUTE_IMAGE_URL = "url"; // required

    public static final String TAG_LOCATION = "location";
    public static final String ATTRIBUTE_LOCATION_LATITUDE = "latitude"; //required
    public static final String ATTRIBUTE_LOCATION_LONGITUDE = "longitude"; //required


    public static final String TAG_AUDIO = "audio";
    public static final String ATTRIBUTE_AUDIO_URL = "url"; // required
    public static final String TAG_TEMPLATE = "template";
    public static final String ATTRIBUTE_TEMPLATE_TYPE = "type"; //reqruied
    public static final String VALUE_TEMPLATE_TYPE_GENERIC = "generic";
    public static final String VALUE_TEMPLATE_TYPE_BUTTON = "button";
    public static final String ATTRIBUTE_TEMPLATE_TITLE = "title";
    public static final String ATTRIBUTE_TEMPLATE_TEXT = "text";
    public static final String ATTRIBUTE_TEMPLATE_IMAGE = "image"; //opt
    public static final String ATTRIBUTE_TEMPLATE_DEFAULT_ACTION = "default_action"; //opt
    public static final String ATTRIBUTE_TEMPLATE_SUBTITLE = "subtitle"; //opt
    public static final String ATTRIBUTE_TEMPLATE_URL = "url"; //opt

    public static final String TAG_BUTTON = "button";
    public static final String ATTRIBUTE_BUTTON_TYPE = "type"; // required
    public static final String ATTRIBUTE_BUTTON_TITLE = "type"; // required
    public static final String ATTRIBUTE_BUTTON_URL = "url";
    public static final String ATTRIBUTE_BUTTON_PAYLOAD = "payload";

    public static final String TAG_CAROUSEL = "carousel";

    public static final String TAG_QUICK_REPLIES = "replies";

    public static String openTag(String tag) {
        return "<"+tag+">";
    }

    public static String closeTag(String tag) {
        return "</"+tag+">";
    }
}
