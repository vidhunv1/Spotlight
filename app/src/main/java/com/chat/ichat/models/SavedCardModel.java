package com.chat.ichat.models;

/**
 * Created by vidhun on 14/07/17.
 */

public class SavedCardModel {
    private String cardNumber;
    private String cardType;
    private String serverId;
    private String merchantHash;
    private String cardToken;

    public SavedCardModel(String cardNumber, String cardType, String serverId) {
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.serverId = serverId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public String getServerId() {
        return serverId;
    }

    public String getMerchantHash() {
        return merchantHash;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void setMerchantHash(String merchantHash) {
        this.merchantHash = merchantHash;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    @Override
    public String toString() {
        return "SavedCardModel{" +
                "cardNumber='" + cardNumber + '\'' +
                ", cardType='" + cardType + '\'' +
                ", serverId='" + serverId + '\'' +
                ", merchantHash='" + merchantHash + '\'' +
                ", cardToken='" + cardToken + '\'' +
                '}';
    }
}
