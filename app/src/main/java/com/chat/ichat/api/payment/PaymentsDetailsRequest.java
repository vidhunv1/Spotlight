package com.chat.ichat.api.payment;

import com.chat.ichat.core.Logger;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vidhun on 14/07/17.
 */

public class PaymentsDetailsRequest {
    @SerializedName("amount")
    private String amount;
    @SerializedName("txnid")
    private String txnid;
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

    public PaymentsDetailsRequest(String amount, String txnid, String productInfo) {
        Logger.d(this, "Amount: "+amount+", txnId: "+txnid+", "+productInfo);
        this.amount = amount;
        this.txnid = txnid;
        this.productInfo = productInfo;
        this.udf1 = "";
        this.udf2 = "";
        this.udf3 = "";
        this.udf4 = "";
        this.udf5 = "";
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setTxnid(String txnid) {
        this.txnid = txnid;
    }

    public void setProductInfo(String productInfo) {
        this.productInfo = productInfo;
    }

    public void setUdf1(String udf1) {
        if(udf1!=null)
            this.udf1 = udf1;
    }

    public void setUdf2(String udf2) {
        if(udf2!=null)
            this.udf2 = udf2;
    }

    public void setUdf3(String udf3) {
        if(udf3!=null)
            this.udf3 = udf3;
    }

    public void setUdf4(String udf4) {
        if(udf4!=null)
            this.udf4 = udf4;
    }

    public void setUdf5(String udf5) {
        if(udf5!=null)
            this.udf5 = udf5;
    }
}
