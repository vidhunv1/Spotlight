package com.stairway.spotlight.screens.message.view_models;

/**
 * Created by vidhun on 26/12/16.
 */

public class TemplateButton{
    private String url;
    private TemplateButton.Type type;
    public String title;
    public String text;
    public String payload;

    public enum Type {
        web_url,
        postback
    }

    public TemplateButton(TemplateButton.Type type, String title) {
        this.type = type;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TemplateButton.Type getType() {
        return type;
    }

    public void setType(TemplateButton.Type type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "TemplateButton{" +
                "url='" + url + '\'' +
                ", type='" + type.name() + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}