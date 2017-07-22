package com.chat.ichat.api.payment;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 13/07/17.
 */

public class MerchantHashRequest {
    @SerializedName("merchant_hash")
    MerchantHashRequest.Hash merchantHash;

    public MerchantHashRequest(String merchantKey, String userCredentials, String cardToken, String merchantHa) {
        merchantHash = new Hash(merchantKey, userCredentials, cardToken, merchantHa);
    }

    public MerchantHashRequest() {
        merchantHash = new Hash();
    }

    public void setCardNumberMasked(String cardNumberMasked) {
        this.merchantHash.setCardNumberMasked(cardNumberMasked);
    }

    public void setCardType(String cardType) {
        this.merchantHash.setCardType(cardType);
    }

    public void setMerchantKey(String merchantKey) {
        this.merchantHash.setMerchantKey(merchantKey);
    }

    public void setCardToken(String cardToken) {
        this.merchantHash.setCardToken(cardToken);
    }

    public void setMerchantHash(String merchantHash) {
        this.merchantHash.setMerchantHash(merchantHash);
    }

    public class Hash {
        @SerializedName("merchant_key")
        String merchantKey;
        @SerializedName("user_credentials")
        String userCredentials;
        @SerializedName("card_token")
        String cardToken;
        @SerializedName("merchant_hash")
        String merchantHash;
        @SerializedName("card_number_masked")
        String cardNumberMasked;
        @SerializedName("card_type")
        String cardType;

        public Hash(String merchantKey, String userCredentials, String cardToken, String merchantHash) {
            this.merchantKey = merchantKey;
            this.userCredentials = userCredentials;
            this.cardToken = cardToken;
            this.merchantHash = merchantHash;
        }

        public Hash() {
        }

        public void setMerchantKey(String merchantKey) {
            this.merchantKey = merchantKey;
        }

        public void setUserCredentials(String userCredentials) {
            this.userCredentials = userCredentials;
        }

        public void setCardToken(String cardToken) {
            this.cardToken = cardToken;
        }

        public void setMerchantHash(String merchantHash) {
            this.merchantHash = merchantHash;
        }

        public void setCardNumberMasked(String cardNumberMasked) {
            this.cardNumberMasked = cardNumberMasked;
        }

        public void setCardType(String cardType) {
            this.cardType = cardType;
        }
    }
}
