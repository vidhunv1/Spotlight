package com.chat.ichat.api.payment;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 14/07/17.
 */

public class PaymentsHashResponse {
    @SerializedName("vas_for_mobile_sdk_hash")
    private String vasForMobileSdkHash;
    @SerializedName("save_user_card_hash")
    private String saveCardHash;
    @SerializedName("payment_related_details_for_mobile_sdk_hash")
    private String paymentRelatedDetailsForMobileSdkHash;
    @SerializedName("payment_hash")
    private String paymentHash;
    @SerializedName("get_user_cards_hash")
    private String getStoredCardsHash;
    @SerializedName("edit_user_card_hash")
    private String editCardHash;
    @SerializedName("delete_user_card_hash")
    private String deleteCardHash;
    @SerializedName("user_credentials")
    private String userCredentials;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("phone")
    private String phone;
    @SerializedName("email")
    private String email;
    @SerializedName("merchant_key")
    private String merchantKey;

    public String getUserCredentials() {
        return userCredentials;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getMerchantKey() {
        return merchantKey;
    }

    public String getVasForMobileSdkHash() {
        return vasForMobileSdkHash;
    }

    public String getSaveCardHash() {
        return saveCardHash;
    }

    public String getPaymentRelatedDetailsForMobileSdkHash() {
        return paymentRelatedDetailsForMobileSdkHash;
    }

    public String getPaymentHash() {
        return paymentHash;
    }

    public String getGetStoredCardsHash() {
        return getStoredCardsHash;
    }

    public String getEditCardHash() {
        return editCardHash;
    }

    public String getDeleteCardHash() {
        return deleteCardHash;
    }

    @Override
    public String toString() {
        return "PaymentsHashResponse{" +
                "vasForMobileSdkHash='" + vasForMobileSdkHash + '\'' +
                ", saveCardHash='" + saveCardHash + '\'' +
                ", paymentRelatedDetailsForMobileSdkHash='" + paymentRelatedDetailsForMobileSdkHash + '\'' +
                ", paymentHash='" + paymentHash + '\'' +
                ", getStoredCardsHash='" + getStoredCardsHash + '\'' +
                ", editCardHash='" + editCardHash + '\'' +
                ", deleteCardHash='" + deleteCardHash + '\'' +
                ", userCredentials='" + userCredentials + '\'' +
                ", firstName='" + firstName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", merchantKey='" + merchantKey + '\'' +
                '}';
    }
}
