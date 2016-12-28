package com.stairway.spotlight.screens.message.view_models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vidhun on 25/12/16.
 */

public class TemplateMessage {
    private String image;
    private String defaultAction;
    private String title;
    private String text;
    private String subtitle;
    private String url;
    private TemplateType type;
    private List<TemplateButton> buttons;
    public enum TemplateType {
        generic,
        button
    }

    public TemplateMessage(TemplateType type, String msg) {
        if(type == TemplateType.generic)
            this.title = msg;
        else if(type == TemplateType.button)
            this.text = msg;
        this.type = type;
        this.buttons = new ArrayList<>();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction) {
        this.defaultAction = defaultAction;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TemplateType getType() {
        return type;
    }

    public void setType(TemplateType type) {
        this.type = type;
    }

    public List<TemplateButton> getButtons() {
        return buttons;
    }

    public void addButton(TemplateButton button) {
        this.buttons.add(button);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
