package com.chat.ichat.screens.payments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.chat.ichat.R;
import com.chat.ichat.api.ApiManager;
import com.chat.ichat.api.StatusResponse;
import com.chat.ichat.api.payment.MerchantHashRequest;
import com.chat.ichat.api.payment.MerchantHashResponse;
import com.chat.ichat.api.payment.PaymentsDetailsRequest;
import com.chat.ichat.api.payment.PaymentsHashResponse;
import com.chat.ichat.core.Logger;
import com.chat.ichat.db.SavedCardsStore;
import com.payu.india.CallBackHandler.OnetapCallback;
import com.payu.india.Interfaces.OneClickPaymentListener;
import com.payu.india.Interfaces.PaymentRelatedDetailsListener;
import com.payu.india.Interfaces.ValueAddedServiceApiListener;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.Payu;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.PostParams.PaymentPostParams;
import com.payu.india.Tasks.GetPaymentRelatedDetailsTask;

import java.util.HashMap;

import de.measite.minidns.record.A;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 14/07/17.
 */

public class PayuPaymentsHelper implements OneClickPaymentListener, PaymentRelatedDetailsListener, ValueAddedServiceApiListener {
    private String amount;
    private String transactionId;
    private String productInfo;
    private String udf1;
    private String udf2;
    private String udf3;
    private String udf4;
    private String udf5;
    private String sUrl;
    private String fUrl;

    private String userCredentials, merchantKey;
    private PaymentParams mPaymentParams;
    private PayuConfig payuConfig;
    private PaymentsInterface paymentsInterface;
    private String maskedCard;
    private String cardType;

    private PostData mPostData;
    private PayuResponse mPayuResponse;

    private Context context;

    public PayuPaymentsHelper(Context context, String amount, String transactionId, String productInfo, String sUrl, String fUrl, PaymentsInterface paymentsInterface) {
        setAmount(amount);
        setTransactionId(transactionId);
        setProductInfo(productInfo);
        this.context = context;
        this.sUrl = sUrl;
        this.fUrl = fUrl;
        this.udf1 = "";
        this.udf2 = "";
        this.udf3 = "";
        this.udf4 = "";
        this.udf5 = "";

        this.paymentsInterface = paymentsInterface;

        String value = "Prod";
        int environment;
        String TEST_ENVIRONMENT = context.getResources().getString(R.string.test);
        if (value.equalsIgnoreCase(TEST_ENVIRONMENT))
            environment = PayuConstants.STAGING_ENV;
        else
            environment = PayuConstants.PRODUCTION_ENV;

        mPaymentParams = new PaymentParams();
        mPaymentParams.setAmount(this.amount);
        mPaymentParams.setProductInfo(this.productInfo);
        mPaymentParams.setTxnId(this.transactionId);
        mPaymentParams.setSurl(this.sUrl);
        mPaymentParams.setFurl(this.fUrl);
        mPaymentParams.setUdf1(udf1);
        mPaymentParams.setUdf2(udf2);
        mPaymentParams.setUdf3(udf3);
        mPaymentParams.setUdf4(udf4);
        mPaymentParams.setUdf5(udf5);

        payuConfig = new PayuConfig();
        payuConfig.setEnvironment(environment);
//        generateHashFromServer(mPaymentParams);

        OnetapCallback.setOneTapCallback(this);
        Payu.setInstance(context);
    }

    public Observable<PaymentsHashResponse> generateHashFromServer(PaymentParams pp) {
        PaymentsDetailsRequest paymentsDetailsRequest = new PaymentsDetailsRequest(pp.getAmount(),pp.getTxnId(), pp.getProductInfo());
        paymentsDetailsRequest.setUdf1(pp.getUdf1());
        paymentsDetailsRequest.setUdf2(pp.getUdf2());
        paymentsDetailsRequest.setUdf3(pp.getUdf3());
        paymentsDetailsRequest.setUdf4(pp.getUdf4());
        paymentsDetailsRequest.setUdf5(pp.getUdf5());

        return ApiManager.getPaymentApi().getPaymentsHash(paymentsDetailsRequest)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(paymentsHashResponse -> {
                    Logger.d(this, "Hashes: "+paymentsHashResponse.toString());

                    merchantKey = paymentsHashResponse.getMerchantKey();
                    userCredentials = paymentsHashResponse.getUserCredentials();

                    mPaymentParams.setKey(paymentsHashResponse.getMerchantKey());
                    mPaymentParams.setUserCredentials(paymentsHashResponse.getUserCredentials());
                    mPaymentParams.setFirstName(paymentsHashResponse.getFirstName());
                    mPaymentParams.setNameOnCard(paymentsHashResponse.getFirstName());
                    mPaymentParams.setPhone(paymentsHashResponse.getPhone());
                    mPaymentParams.setEmail(paymentsHashResponse.getEmail());

                    PayuHashes payuHashes = new PayuHashes();
                    if(paymentsHashResponse.getPaymentHash()!=null && !paymentsHashResponse.getPaymentHash().isEmpty()) {
                        payuHashes.setPaymentHash(paymentsHashResponse.getPaymentHash());
                        mPaymentParams.setHash(paymentsHashResponse.getPaymentHash());
                    }
                    if(paymentsHashResponse.getDeleteCardHash()!=null && !paymentsHashResponse.getDeleteCardHash().isEmpty()) {
                        payuHashes.setDeleteCardHash(paymentsHashResponse.getDeleteCardHash());
                    }
                    if(paymentsHashResponse.getEditCardHash()!=null && !paymentsHashResponse.getEditCardHash().isEmpty()) {
                        payuHashes.setEditCardHash(paymentsHashResponse.getEditCardHash());
                    }
                    if(paymentsHashResponse.getGetStoredCardsHash()!=null && !paymentsHashResponse.getGetStoredCardsHash().isEmpty()) {
                        payuHashes.setStoredCardsHash(paymentsHashResponse.getGetStoredCardsHash());
                    }
                    if(paymentsHashResponse.getPaymentRelatedDetailsForMobileSdkHash()!=null && !paymentsHashResponse.getPaymentRelatedDetailsForMobileSdkHash().isEmpty()) {
                        payuHashes.setPaymentRelatedDetailsForMobileSdkHash(paymentsHashResponse.getPaymentRelatedDetailsForMobileSdkHash());
                    }
                    if(paymentsHashResponse.getSaveCardHash()!=null && !paymentsHashResponse.getSaveCardHash().isEmpty()) {
                        payuHashes.setSaveCardHash(paymentsHashResponse.getSaveCardHash());
                    }
                    if(paymentsHashResponse.getVasForMobileSdkHash()!=null && !paymentsHashResponse.getVasForMobileSdkHash().isEmpty()) {
                        payuHashes.setVasForMobileSdkHash(paymentsHashResponse.getVasForMobileSdkHash());
                    }
                });
    }

    private void storeMerchantHash(String cardToken, String merchantHash) {
        MerchantHashRequest merchantHashRequest = new MerchantHashRequest(merchantKey, userCredentials, cardToken, merchantHash);
        merchantHashRequest.setCardNumberMasked(maskedCard);
        merchantHashRequest.setCardType(cardType);
        ApiManager.getPaymentApi().storePaymentCardHash(merchantHashRequest)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MerchantHashResponse>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(MerchantHashResponse merchantHashResponse) {
                        Logger.d(this, "Stored card :"+merchantHashResponse.getMerchantHashes().size()+", "+merchantHashResponse.getMerchantHashes().get(0).toString());
                        MerchantHashResponse.Hash j = merchantHashResponse.getMerchantHashes().get(0);

                        SavedCardsStore.getInstance().putCard(j.getCardNumberMasked(), j.getCardType(), j.getServerId())
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<Boolean>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onNext(Boolean aBoolean) {

                                    }
                                });
                    }
                });
    }

    private void deleteMerchantHash(String cardToken) {
        ApiManager.getPaymentApi().deletePaymentCardHash(cardToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StatusResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(StatusResponse statusResponse) {}
                });
    }

    public HashMap<String, String> getAllOneClickHashHelper(String merchantKey, String userCredentials) {
        Logger.d(this, "getAllOneClickHashHelper");

        return ApiManager.getPaymentApi().getPaymentCardHash(merchantKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(merchantHashResponse -> {
                    HashMap<String, String> cardTokens = new HashMap<String, String>();
                    for (MerchantHashResponse.Hash hash : merchantHashResponse.getMerchantHashes()) {
                        Logger.d(this, "Stored Hash: "+hash.getCardToken()+", "+hash.getMerchantHash());
                        cardTokens.put(hash.getCardToken(), hash.getMerchantHash());
                    }
                    return cardTokens;
                })
                .toBlocking().first();
    }

    /**
     * Returns a HashMap object of cardToken and one click hash from merchant server.
     * <p>
     * This method will be called as a async task, regardless of merchant implementation.
     * Hence, not to call this function as async task.
     * The function should return a cardToken and corresponding one click hash as a hashMap.
     *
     * @param userCreds a string giving the user credentials of user.
     * @return the Hash Map of cardToken and one Click hash.
     **/
    @Override
    public HashMap<String, String> getAllOneClickHash(String userCreds) {
        Logger.d(this, "get all one click");
        // 1. GET http request from your server
        // GET params - merchant_key, user_credentials.
        // 2. In response we get a
        // this is a sample code for fetching one click hash from merchant server.
        return getAllOneClickHashHelper(merchantKey, userCreds);
    }

    @Override
    public void getOneClickHash(String cardToken, String merchantKey, String userCredentials) {
        Logger.d(this, "get one click");
    }
    /**
     * This method will be called as a async task, regardless of merchant implementation.
     * Hence, not to call this function as async task.
     * This function save the oneClickHash corresponding to its cardToken
     *
     * @param cardToken    a string containing the card token
     * @param oneClickHash a string containing the one click hash.
     **/

    @Override
    public void saveOneClickHash(String cardToken, String oneClickHash) {
        Logger.d(this, "Save one click");
        // 1. POST http request to your server
        // POST params - merchant_key, user_credentials,card_token,merchant_hash.
        // 2. In this POST method the oneclickhash is stored corresponding to card token in merchant server.
        // this is a sample code for storing one click hash on merchant server.
        storeMerchantHash(cardToken, oneClickHash);
    }

    /**
     * This method will be called as a async task, regardless of merchant implementation.
     * Hence, not to call this function as async task.
     * This function delete’s the oneClickHash from the merchant server
     *
     * @param cardToken       a string containing the card token
     * @param userCredentials a string containing the user credentials.
     **/

    @Override
    public void deleteOneClickHash(String cardToken, String userCredentials) {
        Logger.d(this, "delete one click");
        // 1. POST http request to your server
        // POST params  - merchant_hash.
        // 2. In this POST method the oneclickhash is deleted in merchant server.
        // this is a sample code for deleting one click hash from merchant server.
        deleteMerchantHash(cardToken);
    }

    public void makePaymentByCreditCard(String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        maskedCard = cardNumber.substring(0, 6) + "XXXXXX" +cardNumber.substring(11, 16);
        CardTypeHelper cardTypeHelper = CardTypeHelper.detect(cardNumber);
        if(cardTypeHelper == CardTypeHelper.VISA) {
            cardType = "VISA";
        } else if(cardTypeHelper == CardTypeHelper.MASTERCARD) {
            cardType = "MAST";
        } else if(cardTypeHelper == CardTypeHelper.AMERICAN_EXPRESS) {
            cardType = "AMEX";
        } else if(cardTypeHelper == CardTypeHelper.DINERS_CLUB) {
            cardType = "DINERS";
        } else if(cardTypeHelper == CardTypeHelper.DISCOVER) {
            cardType = "DISCOVER";
        } else if(cardTypeHelper == CardTypeHelper.JCB) {
            cardType = "JCB";
        }
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        generateHashFromServer(mPaymentParams)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PaymentsHashResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(PaymentsHashResponse paymentsHashResponse) {
                        Logger.d(this, "MerchantKey: "+mPaymentParams.getKey());
                        progressDialog.dismiss();
                        mPaymentParams.setStoreCard(1);
                        mPaymentParams.setEnableOneClickPayment(1);// TODO set flag for one tap payment

                        // lets try to get the post params
                        mPaymentParams.setCardNumber(cardNumber);
                        mPaymentParams.setNameOnCard(mPaymentParams.getFirstName());
                        mPaymentParams.setExpiryMonth(expiryMonth);
                        mPaymentParams.setExpiryYear(expiryYear);
                        mPaymentParams.setCvv(cvv);
                        mPaymentParams.setCardName(mPaymentParams.getFirstName());

                        try {
                            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        payuConfig.setData(mPostData.getResult());
                        Logger.d(this, "PayuConfig: "+payuConfig.getData()+", "+payuConfig.getConfig().getStatus()+", "+payuConfig.getData().toString());

                        Intent intent = new Intent(context, PaymentsActivity.class);
                        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);

                        paymentsInterface.startPaymentsActivity(intent, PayuConstants.PAYU_REQUEST_CODE);
                    }
                });
    }

    public void makePaymentByStoredCard(String cardToken, String merchantHash) {
        Logger.d(this, "makePaymentByStoredCard: "+cardToken+", "+merchantHash);
        PayuPaymentsHelper payuPaymentsHelper = PayuPaymentsHelper.this;
        generateHashFromServer(mPaymentParams)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PaymentsHashResponse>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(PaymentsHashResponse paymentsHashResponse) {
                        MerchantWebService merchantWebService = new MerchantWebService();
                        merchantWebService.setKey(mPaymentParams.getKey());
                        merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
                        merchantWebService.setVar1(mPaymentParams.getUserCredentials());

                        merchantWebService.setHash(paymentsHashResponse.getPaymentRelatedDetailsForMobileSdkHash());
                        mPaymentParams.setCardToken(cardToken);
                        mPaymentParams.setCardCvvMerchant(merchantHash);
                        mPaymentParams.setEnableOneClickPayment(1);

                        PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
                        if (postData.getCode() == PayuErrors.NO_ERROR) {
                            // ok we got the post params, let make an api call to payu to fetch the payment related details
                            payuConfig.setData(postData.getResult());

                            GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask = new GetPaymentRelatedDetailsTask(payuPaymentsHelper);
                            paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
                        } else {
                            Toast.makeText(context, postData.getResult(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        mPayuResponse = payuResponse;
        if(mPayuResponse.getStoredCards()==null || mPayuResponse.getStoredCards().size()==0 ) {
            //possible no internet connection
            return;
        }
        Logger.d(this, payuResponse.getStoredCards().size()+":: onPaymentRelatedDetailsResponse: "+payuResponse.getStoredCards().get(0).getMaskedCardNumber());
        StoredCard storedCard = mPayuResponse.getStoredCards().get(0);

        for (StoredCard card : mPayuResponse.getStoredCards()) {
            Logger.d(this, card.getCardToken()+" == "+mPaymentParams.getCardToken());
            if(card.getCardToken().equals(mPaymentParams.getCardToken())) {
                storedCard = card;
                Logger.d(this, "Cardd: "+card.getMaskedCardNumber()+", "+card.getCvv());
                break;
            }
        }

        mPaymentParams.setCardToken(storedCard.getCardToken());
        mPaymentParams.setNameOnCard("VV");
        mPaymentParams.setCardName("VV");
        mPaymentParams.setExpiryMonth(storedCard.getExpiryMonth());
        mPaymentParams.setExpiryYear(storedCard.getExpiryYear());
        mPaymentParams.setCardNumber(storedCard.getMaskedCardNumber());

        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.CC).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

        payuConfig.setData(mPostData.getResult());
        Logger.d(this, "PayuConfig: "+payuConfig.getData()+", "+payuConfig.getConfig().getStatus()+", "+payuConfig.getData().toString());

        Intent intent = new Intent(context, PaymentsActivity.class);
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);

        paymentsInterface.startPaymentsActivity(intent, PayuConstants.PAYU_REQUEST_CODE);
    }

    @Override
    public void onValueAddedServiceApiResponse(PayuResponse payuResponse) {

    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setProductInfo(String productInfo) {
        this.productInfo = productInfo;
    }

    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    public void setUdf4(String udf4) {
        this.udf4 = udf4;
    }

    public void setUdf5(String udf5) {
        this.udf5 = udf5;
    }

    public interface PaymentsInterface {
        void startPaymentsActivity(Intent intent, int requestCode);
    }
}
