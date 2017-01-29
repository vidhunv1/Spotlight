package com.stairway.spotlight;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Set;

/**
 * Created by vidhun on 29/01/17.
 */

public class AccessToken {
    private final Date expires;
    private final String accessToken;
    private final String userName;

    public AccessToken(String accessToken, String userName, Date expires) {
        this.expires = expires;
        this.accessToken = accessToken;
        this.userName = userName;
    }

    public Date getExpires() {
        return expires;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isExpired() {
        return new Date().after(this.expires);
    }


}
