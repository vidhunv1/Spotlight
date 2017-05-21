package com.chat.ichat.api.location;

import com.chat.ichat.api.ErrorResponse;
import com.chat.ichat.api.user._User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 21/05/17.
 */

public class NearbyPeopleResponse {
    @SerializedName("error")
    ErrorResponse error;
    @SerializedName("contacts")
    @Expose
    private List<UserLocation> nearbyPeople;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;

    public boolean isSuccess() {
        return error==null;
    }

    public ErrorResponse getError() {
        return error;
    }

    public List<UserLocation> getNearbyPeople() {
        return nearbyPeople;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
