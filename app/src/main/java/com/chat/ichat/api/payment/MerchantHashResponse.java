package com.chat.ichat.api.payment;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vidhun on 13/07/17.
 */

public class MerchantHashResponse {
    @SerializedName("merchant_hashes")
    List<Hash> merchantHashes;

    public List<Hash> getMerchantHashes() {
        return merchantHashes;
    }

    @Override
    public String toString() {
        String str = "";
        if(merchantHashes!=null && !merchantHashes.isEmpty()) {
            for (MerchantHashResponse.Hash hash : merchantHashes) {
                str = str + "\n" + hash;
            }
        }
        return "MerchantHashResponse: "+str;
    }

    public class Hash {
        @SerializedName("merchant_hash")
        String merchantHash;
        @SerializedName("card_token")
        String cardToken;
        @SerializedName("card_number_masked")
        String cardNumberMasked;
        @SerializedName("card_type")
        String cardType;
        @SerializedName("merchant_key")
        String merchantKey;
        @SerializedName("user_credentials")
        String userCredentials;
        @SerializedName("server_id")
        int serverId;

        public String getMerchantHash() {
            return merchantHash;
        }

        public String getCardToken() {
            return cardToken;
        }

        public String getCardNumberMasked() {
            return cardNumberMasked;
        }

        public String getCardType() {
            return cardType;
        }

        public String getMerchantKey() {
            return merchantKey;
        }

        public String getUserCredentials() {
            return userCredentials;
        }

        public int getServerId() {
            return serverId;
        }

        @Override
        public String toString() {
            return "Hash{" +
                    "merchantHash='" + merchantHash + '\'' +
                    ", cardToken='" + cardToken + '\'' +
                    ", cardNumberMasked='" + cardNumberMasked + '\'' +
                    ", cardType='" + cardType + '\'' +
                    ", merchantKey='" + merchantKey + '\'' +
                    ", userCredentials='" + userCredentials + '\'' +
                    ", serverId='" + serverId + '\'' +
                    '}';
        }
    }
}
