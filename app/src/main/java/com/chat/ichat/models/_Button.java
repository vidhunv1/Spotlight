package com.chat.ichat.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 08/02/17.
 */

public class _Button {
    @SerializedName("type")
    private Type type;
    @SerializedName("url")
    private String url;
    @SerializedName("title")
    private String title;
    @SerializedName("payload")
    private String payload;
    @SerializedName("payment_summary")
    private PaymentSummary paymentSummary;

    public enum Type {
        @SerializedName("web_url")
        web_url,
        @SerializedName("postback")
        postback,
        @SerializedName("payment")
        payment
    }

    public Type getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getPayload() {
        return payload;
    }

    public PaymentSummary getPaymentSummary() {
        return paymentSummary;
    }

    @Override
    public String toString() {
        return "_Button{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }

    public class PaymentSummary {
        @SerializedName("amount")
        private String amount;
        @SerializedName("txnid")
        private String transactionId;
        @SerializedName("product_info")
        private String productInfo;
        @SerializedName("udf1")
        private String udf1;
        @SerializedName("udf2")
        private String udf2;
        @SerializedName("udf3")
        private String udf3;
        @SerializedName("udf4")
        private String udf4;
        @SerializedName("udf5")
        private String udf5;
        @SerializedName("surl")
        private String sUrl;
        @SerializedName("furl")
        private String fUrl;

        public String getAmount() {
            return amount;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getProductInfo() {
            return productInfo;
        }

        public String getUdf1() {
            return udf1;
        }

        public String getUdf2() {
            return udf2;
        }

        public String getUdf3() {
            return udf3;
        }

        public String getUdf4() {
            return udf4;
        }

        public String getUdf5() {
            return udf5;
        }

        public String getsUrl() {
            return sUrl;
        }

        public String getfUrl() {
            return fUrl;
        }
    }
}
