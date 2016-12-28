package com.stairway.spotlight.screens.message.view_models;

/**
 * Created by vidhun on 26/12/16.
 */

public class LocationMessage {
    private String latitude;
    private String longitude;

    public LocationMessage(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
