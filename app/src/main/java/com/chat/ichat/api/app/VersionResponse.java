package com.chat.ichat.api.app;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.chat.ichat.api.ErrorResponse;

/**
 * Created by vidhun on 10/04/17.
 */

public class VersionResponse {
    @SerializedName("error")
    ErrorResponse error;
    @SerializedName("version_code")
    @Expose
    private int versionCode;
    @SerializedName("version_name")
    @Expose
    private String versionName;
    @SerializedName("is_mandatory")
    @Expose
    private boolean isMandatory;

    public ErrorResponse getError() {
        return error;
    }

    public void setError(ErrorResponse error) {
        this.error = error;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }
}
