package com.chat.ichat.api.location;

import com.chat.ichat.api.user._User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 21/05/17.
 */

public class UserLocation {
    @SerializedName("user")
    @Expose
    private _User user;
    @SerializedName("latitude")
    @Expose
    double latitude;
    @SerializedName("longitude")
    @Expose
    double longitude;
    @SerializedName("distance")
    @Expose
    double distance;

    public _User getUser() {
        return user;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "user=" + user +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", distance=" + distance +
                '}';
    }
}
