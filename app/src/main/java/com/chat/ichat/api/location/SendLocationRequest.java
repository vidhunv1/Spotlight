package com.chat.ichat.api.location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 21/05/17.
 */

public class SendLocationRequest {
    @SerializedName("latitude")
    @Expose
    double latitude;
    @SerializedName("longitude")
    @Expose
    double longitude;

    public SendLocationRequest(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
