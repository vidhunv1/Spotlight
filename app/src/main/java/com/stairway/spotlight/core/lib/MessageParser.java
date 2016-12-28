package com.stairway.spotlight.core.lib;

import com.stairway.data.config.Logger;
import com.stairway.spotlight.screens.message.view_models.AudioMessage;
import com.stairway.spotlight.screens.message.view_models.LocationMessage;
import com.stairway.spotlight.screens.message.view_models.TemplateButton;
import com.stairway.spotlight.screens.message.view_models.TemplateMessage;
import com.stairway.spotlight.screens.message.view_models.TextMessage;
import com.stairway.spotlight.screens.message.view_models.VideoMessage;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by vidhun on 25/12/16.
 */
//TODO: ugly code!! change it soon
public class MessageParser {
    public static final String TAG_HEAD = "message";
    public static final String TAG_TEXT = "text";
    public static final String TAG_VIDEO = "video";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_AUDIO = "audio";
    public static final String TAG_TEMPLATE = "template";
    public static final String TAG_BUTTON = "button";
    public static final String TAG_CAROUSEL = "carousel";
    public static final String TAG_QUICK_REPLIES = "replies";

    public static final String ATTRIBUTE_VIDEO_URL = "url"; // required
    public static final String ATTRIBUTE_AUDIO_URL = "url"; // required
    public static final String ATTRIBUTE_LOCATION_LATITUDE = "latitude"; //required
    public static final String ATTRIBUTE_LOCATION_LONGITUDE = "longitude"; //required
    public static final String ATTRIBUTE_TEMPLATE_TYPE = "type"; //reqruied
    public static final String ATTRIBUTE_TEMPLATE_TITLE = "title";
    public static final String ATTRIBUTE_TEMPLATE_TEXT = "text";
    public static final String ATTRIBUTE_TEMPLATE_IMAGE = "image"; //opt
    public static final String ATTRIBUTE_TEMPLATE_DEFAULT_ACTION = "default_action"; //opt
    public static final String ATTRIBUTE_TEMPLATE_SUBTITLE = "subtitle"; //opt
    public static final String ATTRIBUTE_TEMPLATE_URL = "url"; //opt
    public static final String ATTRIBUTE_BUTTON_TYPE = "type"; // required
    public static final String ATTRIBUTE_BUTTON_TITLE = "type"; // required
    public static final String ATTRIBUTE_BUTTON_URL = "url";
    public static final String ATTRIBUTE_BUTTON_PAYLOAD = "payload";

    public static final String VALUE_TEMPLATE_TYPE_GENERIC = "generic";
    public static final String VALUE_TEMPLATE_TYPE_BUTTON = "button";

    public enum MessageType {
        text,
        video,
        audio,
        location,
        template
    }

    private String messageXml;
    private MessageType messageType;

    public MessageParser(String messageXml){
        this.messageXml = messageXml;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Object parseMessage() throws ParseException {
        if(messageXml.isEmpty() || messageXml==null)
            throw new IllegalStateException("MessageXml is null");
        if(!messageXml.startsWith("<"+TAG_HEAD+">")) {
            this.messageType = MessageType.text;
            return new TextMessage(messageXml);
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(messageXml));
            try {
                Document doc = documentBuilder.parse(is);

                String parent = doc.getDocumentElement().getTagName();
                if(parent.equals(TAG_HEAD)) {
                    NodeList messageList = doc.getElementsByTagName(TAG_HEAD).item(0).getChildNodes();
                    for (int i = 0; i < messageList.getLength(); i++) {
                        Node node = messageList.item(i);
                        if(node.getNodeName().equals(TAG_TEXT)) {
                            this.messageType = MessageType.text;
                            return parseText(node);
                        } else if(node.getNodeName().equals(TAG_VIDEO)) {
                            this.messageType = MessageType.video;
                            return parseVideo(node);
                        }  else if(node.getNodeName().equals(TAG_LOCATION)) {
                            this.messageType = MessageType.location;
                            return parseLocation(node);
                        }  else if(node.getNodeName().equals(TAG_AUDIO)) {
                            this.messageType = MessageType.audio;
                            return parseAudio(node);
                        } else if(node.getNodeName().equals(TAG_TEMPLATE)) {
                            this.messageType = MessageType.template;
                            return parseTemplate(node);
                        } else {
                            throw new ParseException("Malformed xml :" + parent, 116);
                        }
                    }
                } else {
                    throw new ParseException("Malformed xml: "+parent,0);
                }
            } catch (Exception e) {
                throw new ParseException("Malformed xml",0);
            }
        } catch (ParserConfigurationException e1) {
            throw new ParseException("Malformed xml",0);
        }
        return null;
    }

    public List<String> parseQuickReplies() throws ParseException{
        if(messageXml.isEmpty() || messageXml==null)
            throw new IllegalStateException("MessageXml is null");
        List<String> replies = new ArrayList<>();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(messageXml));
            try {
                Document doc = documentBuilder.parse(is);

                String parent = doc.getDocumentElement().getTagName();
                if(parent.equals(TAG_HEAD)) {
                    NodeList messageList = doc.getElementsByTagName(TAG_QUICK_REPLIES).item(0).getChildNodes();
                    for (int i = 0; i < messageList.getLength(); i++) {
                        Node node = messageList.item(i);
                        if(node.getNodeName().equals(TAG_TEXT)) {
                            replies.add(parseText(node).getText());
                            }
                        }
                    }
            } catch (Exception e) {
                throw new ParseException("Malformed xml",0);
            }
        } catch (ParserConfigurationException e1) {
            throw new ParseException("Malformed xml",0);
        }
        return replies;
    }

    private TextMessage parseText(Node node) {
        if(node.getTextContent()!=null)
            return new TextMessage(node.getTextContent());
        return null;
    }

    private VideoMessage parseVideo(Node node) throws ParseException{
        NamedNodeMap attributes = node.getAttributes();
        if(attributes.getNamedItem(ATTRIBUTE_VIDEO_URL)==null)
            throw new ParseException("Malformed xml: video url is required",0);

        return new VideoMessage(attributes.getNamedItem(ATTRIBUTE_VIDEO_URL).getTextContent());
    }

    private LocationMessage parseLocation(Node node) throws ParseException{
        NamedNodeMap attributes = node.getAttributes();

        if(attributes.getNamedItem(ATTRIBUTE_LOCATION_LATITUDE)==null || attributes.getNamedItem(ATTRIBUTE_LOCATION_LONGITUDE)==null)
            throw new ParseException("Malformed xml: lat and long is required",0);
        String latitude = attributes.getNamedItem(ATTRIBUTE_LOCATION_LATITUDE).getTextContent();
        String longitude = attributes.getNamedItem(ATTRIBUTE_LOCATION_LONGITUDE).getTextContent();

        return new LocationMessage(latitude, longitude);
    }

    private AudioMessage parseAudio(Node node) throws ParseException{
        NamedNodeMap attributes = node.getAttributes();

        if(attributes.getNamedItem(ATTRIBUTE_AUDIO_URL)==null)
            throw new ParseException("Malformed xml: audio url is required",0);
        String audioUrl = attributes.getNamedItem(ATTRIBUTE_VIDEO_URL).getTextContent();

        return new AudioMessage(audioUrl);
    }

    private TemplateMessage parseTemplate(Node node) throws ParseException{
        NamedNodeMap attributes = node.getAttributes();
        TemplateMessage templateMessage;

        if(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_TYPE)==null)
            throw new ParseException("Malformed xml: type",0);
        String type = attributes.getNamedItem(ATTRIBUTE_TEMPLATE_TYPE).getTextContent();

        switch (type) {
            case VALUE_TEMPLATE_TYPE_GENERIC:
                if(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_TITLE) == null)
                    throw new ParseException("Title is required", 0);
                templateMessage = new TemplateMessage(TemplateMessage.TemplateType.generic, attributes.getNamedItem(ATTRIBUTE_TEMPLATE_TITLE).getTextContent());
                break;
            case VALUE_TEMPLATE_TYPE_BUTTON:
                if(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_TEXT) == null)
                    throw new ParseException("Text is required", 0);
                templateMessage = new TemplateMessage(TemplateMessage.TemplateType.button, attributes.getNamedItem(ATTRIBUTE_TEMPLATE_TEXT).getTextContent());
                break;
            default:
                throw new ParseException("Malformed xml: type and title are required", 0);
        }

        if(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_IMAGE)!=null)
            templateMessage.setImage(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_IMAGE).getTextContent());

        if(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_DEFAULT_ACTION)!=null)
            templateMessage.setDefaultAction(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_DEFAULT_ACTION).getTextContent());

        if(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_SUBTITLE)!=null)
            templateMessage.setSubtitle(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_SUBTITLE).getTextContent());

        if(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_URL)!=null)
            templateMessage.setUrl(attributes.getNamedItem(ATTRIBUTE_TEMPLATE_URL).getTextContent());

        NodeList buttonList = node.getChildNodes();
        for (int i = 0; i < buttonList.getLength(); i++) {
            TemplateButton templateButton = parseButton(buttonList.item(i));
            if(templateButton!=null)
                templateMessage.addButton(templateButton);
        }

        return templateMessage;
    }

    private TemplateButton parseButton(Node node) {
        String title;
        TemplateButton.Type type;
        TemplateButton button;

        if(!node.getNodeName().equals(TAG_BUTTON))
            return null;
        NamedNodeMap attributes = node.getAttributes();
        if(attributes.getNamedItem(ATTRIBUTE_BUTTON_TYPE)==null)
            return null;

        if(attributes.getNamedItem(ATTRIBUTE_BUTTON_TYPE).getTextContent().equals(TemplateButton.Type.web_url.name()))
            type = TemplateButton.Type.web_url;
        else if(attributes.getNamedItem(ATTRIBUTE_BUTTON_TYPE).getTextContent().equals(TemplateButton.Type.postback.name()))
            type = TemplateButton.Type.postback;
        else
            return null;

        if(!node.getTextContent().isEmpty())
            title = node.getTextContent();
        else
            title = attributes.getNamedItem(ATTRIBUTE_BUTTON_TITLE).getTextContent();

        button = new TemplateButton(type, title);

        if(type == TemplateButton.Type.web_url)
            button.setUrl(attributes.getNamedItem(ATTRIBUTE_BUTTON_URL).getTextContent());
        if(type == TemplateButton.Type.postback)
            button.setPayload(attributes.getNamedItem(ATTRIBUTE_BUTTON_PAYLOAD).getTextContent());

        return button;
    }
}