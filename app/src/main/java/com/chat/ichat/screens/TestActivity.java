//package com.chat.ichat.screens;
//
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.Spinner;
//import android.widget.Toast;
//
//import com.chat.ichat.R;
//import com.chat.ichat.api.ApiManager;
//import com.chat.ichat.api.StatusResponse;
//import com.chat.ichat.api.payment.MerchantHashRequest;
//import com.chat.ichat.api.payment.MerchantHashResponse;
//import com.chat.ichat.api.payment.PaymentsDetailsRequest;
//import com.chat.ichat.api.payment.PaymentsHashResponse;
//import com.chat.ichat.core.Logger;
//import com.payu.india.CallBackHandler.OnetapCallback;
//import com.payu.india.Extras.PayUChecksum;
//import com.payu.india.Extras.PayUSdkDetails;
//import com.payu.india.Interfaces.OneClickPaymentListener;
//import com.payu.india.Model.PaymentParams;
//import com.payu.india.Model.PayuConfig;
//import com.payu.india.Model.PayuHashes;
//import com.payu.india.Model.PostData;
//import com.payu.india.Payu.Payu;
//import com.payu.india.Payu.PayuConstants;
//import com.payu.india.Payu.PayuErrors;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.UnsupportedEncodingException;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.ProtocolException;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Iterator;
//
//import rx.Subscriber;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//
///**
// * Created by vidhun on 04/07/17.
// */
//public class TestActivity extends AppCompatActivity implements OneClickPaymentListener {
//    private String userCredentials, merchantKey;
//    // These will hold all the payment parameters
//    private PaymentParams mPaymentParams;
//
//    // This sets the configuration
//    private PayuConfig payuConfig;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_test);
//
//        OnetapCallback.setOneTapCallback(this);
//        Payu.setInstance(this);
//
//        navigateToBaseActivity();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
//        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
//            if (data != null) {
//
//                /**
//                 * Here, data.getStringExtra("payu_response") ---> Implicit response sent by PayU
//                 * data.getStringExtra("result") ---> Response received from merchant's Surl/Furl
//                 *
//                 * PayU sends the same response to merchant server and in app. In response check the value of key "status"
//                 * for identifying status of transaction. There are two possible status like, success or failure
//                 * */
//                new AlertDialog.Builder(this)
//                        .setCancelable(false)
//                        .setMessage("Payu's Data : " + data.getStringExtra("payu_response") + "\n\n\n Merchant's Data: " + data.getStringExtra("result"))
//                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                dialog.dismiss();
//                            }
//                        }).show();
//
//            } else {
//                Toast.makeText(this, getString(R.string.could_not_receive_data), Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    /**
//     * This method prepares all the payments params to be sent to PayuBaseActivity.java
//     */
//    public void navigateToBaseActivity() {
//
//        String amount = "10";
//        String productInfo = "asdasadsd";
//        String transactionId = "" + System.currentTimeMillis();
//        String sUrl = "https://payu.herokuapp.com/success";
//        String fUrl = "https://payu.herokuapp.com/failure";
//
//        String value = "Test";
//        int environment;
//        String TEST_ENVIRONMENT = getResources().getString(R.string.test);
//        if (value.equals(TEST_ENVIRONMENT))
//            environment = PayuConstants.STAGING_ENV;
//        else
//            environment = PayuConstants.PRODUCTION_ENV;
//
//        //TODO Below are mandatory params for hash genetationvxde2n4B
//        mPaymentParams = new PaymentParams();
//        /**
//         * For Test Environment, merchantKey = "gtKFFx"
//         * For Production Environment, merchantKey should be your live key or for testing in live you can use "0MQaQP"
//         */
//
//        mPaymentParams.setAmount(amount);
//        mPaymentParams.setProductInfo(productInfo);
//
//        /*
//        * Transaction Id should be kept unique for each transaction.
//        * */
//        mPaymentParams.setTxnId(transactionId);
//
//        /**
//         * Surl --> Success url is where the transaction response is posted by PayU on successful transaction
//         * Furl --> Failre url is where the transaction response is posted by PayU on failed transaction
//         */
//        mPaymentParams.setSurl(sUrl);
//        mPaymentParams.setFurl(fUrl);
//
//        /*
//         * udf1 to udf5 are options params where you can pass additional information related to transaction.
//         * If you don't want to use it, then send them as empty string like, udf1=""
//         * */
//        mPaymentParams.setUdf1("");
//        mPaymentParams.setUdf2("");
//        mPaymentParams.setUdf3("");
//        mPaymentParams.setUdf4("");
//        mPaymentParams.setUdf5("");
//
//        //TODO Sets the payment environment in PayuConfig object
//        payuConfig = new PayuConfig();
//        payuConfig.setEnvironment(environment);
//
//        generateHashFromServer(mPaymentParams);
//    }
//
//    /**
//     * This method generates hash from server.
//     *
//     * @param mPaymentParams payments params used for hash generation
//     */
//    public void generateHashFromServer(PaymentParams mPaymentParams) {
//        PaymentsDetailsRequest paymentsDetailsRequest = new PaymentsDetailsRequest(mPaymentParams.getAmount(),mPaymentParams.getTxnId(), mPaymentParams.getProductInfo());
//        paymentsDetailsRequest.setUdf1(mPaymentParams.getUdf1());
//        paymentsDetailsRequest.setUdf2(mPaymentParams.getUdf2());
//        paymentsDetailsRequest.setUdf3(mPaymentParams.getUdf3());
//        paymentsDetailsRequest.setUdf4(mPaymentParams.getUdf4());
//        paymentsDetailsRequest.setUdf5(mPaymentParams.getUdf5());
//
//        ProgressDialog progressDialog = new ProgressDialog(TestActivity.this);
//        progressDialog.setMessage("Please wait...");
//        progressDialog.show();
//
//        ApiManager.getPaymentApi().getPaymentsHash(paymentsDetailsRequest)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<PaymentsHashResponse>() {
//                    @Override
//                    public void onCompleted() {}
//
//                    @Override
//                    public void onError(Throwable e) {}
//
//                    @Override
//                    public void onNext(PaymentsHashResponse paymentsHashResponse) {
//                        Logger.d(this, "Hashes: "+paymentsHashResponse.toString());
//
//                        merchantKey = paymentsHashResponse.getMerchantKey();
//                        userCredentials = paymentsHashResponse.getUserCredentials();
//
//                        mPaymentParams.setKey(paymentsHashResponse.getMerchantKey());
//                        mPaymentParams.setUserCredentials(paymentsHashResponse.getUserCredentials());
//                        mPaymentParams.setFirstName(paymentsHashResponse.getFirstName());
//                        mPaymentParams.setNameOnCard(paymentsHashResponse.getFirstName());
//                        mPaymentParams.setPhone(paymentsHashResponse.getPhone());
//                        mPaymentParams.setEmail(paymentsHashResponse.getEmail());
//
//                        PayuHashes payuHashes = new PayuHashes();
//                        if(paymentsHashResponse.getPaymentHash()!=null && !paymentsHashResponse.getPaymentHash().isEmpty()) {
//                            payuHashes.setPaymentHash(paymentsHashResponse.getPaymentHash());
//                        }
//                        if(paymentsHashResponse.getDeleteCardHash()!=null && !paymentsHashResponse.getDeleteCardHash().isEmpty()) {
//                            payuHashes.setDeleteCardHash(paymentsHashResponse.getDeleteCardHash());
//                        }
//                        if(paymentsHashResponse.getEditCardHash()!=null && !paymentsHashResponse.getEditCardHash().isEmpty()) {
//                            payuHashes.setEditCardHash(paymentsHashResponse.getEditCardHash());
//                        }
//                        if(paymentsHashResponse.getGetStoredCardsHash()!=null && !paymentsHashResponse.getGetStoredCardsHash().isEmpty()) {
//                            payuHashes.setStoredCardsHash(paymentsHashResponse.getGetStoredCardsHash());
//                        }
//                        if(paymentsHashResponse.getPaymentRelatedDetailsForMobileSdkHash()!=null && !paymentsHashResponse.getPaymentRelatedDetailsForMobileSdkHash().isEmpty()) {
//                            payuHashes.setPaymentRelatedDetailsForMobileSdkHash(paymentsHashResponse.getPaymentRelatedDetailsForMobileSdkHash());
//                        }
//                        if(paymentsHashResponse.getSaveCardHash()!=null && !paymentsHashResponse.getSaveCardHash().isEmpty()) {
//                            payuHashes.setSaveCardHash(paymentsHashResponse.getSaveCardHash());
//                        }
//                        if(paymentsHashResponse.getVasForMobileSdkHash()!=null && !paymentsHashResponse.getVasForMobileSdkHash().isEmpty()) {
//                            payuHashes.setVasForMobileSdkHash(paymentsHashResponse.getVasForMobileSdkHash());
//                        }
//                        progressDialog.dismiss();
//                        launchSdkUI(payuHashes);
//                    }
//                });
//    }
//
//    /**
//     * This method adds the Payuhashes and other required params to intent and launches the PayuBaseActivity.java
//     *
//     * @param payuHashes it contains all the hashes generated from merchant server
//     */
//    public void launchSdkUI(PayuHashes payuHashes) {
//        Intent intent = new Intent(this, PayUBaseActivity.class);
//        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
//        intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);
//        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);
//
//        //Lets fetch all the one click card tokens first
//        fetchMerchantHashes(intent);
//    }
//
//    /**
//     * This method stores merchantHash and cardToken on merchant server.
//     *
//     * @param cardToken    card token received in transaction response
//     * @param merchantHash merchantHash received in transaction response
//     */
//    private void storeMerchantHash(String cardToken, String merchantHash) {
//        MerchantHashRequest merchantHashRequest = new MerchantHashRequest(merchantKey, userCredentials, cardToken, merchantHash);
//        ApiManager.getPaymentApi().storePaymentCardHash(merchantHashRequest)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<MerchantHashResponse>() {
//                    @Override
//                    public void onCompleted() {}
//                    @Override
//                    public void onError(Throwable e) {}
//
//                    @Override
//                    public void onNext(MerchantHashResponse statusResponse) {
//                        Logger.d(this, "Stored card");
//                    }
//                });
//    }
//
//    /**
//     * This method fetches merchantHash and cardToken already stored on merchant server.
//     */
//    private void fetchMerchantHashes(final Intent intent) {
//        // now make the api call.
//        final String postParams = "merchant_key=" + merchantKey + "&user_credentials=" + userCredentials;
//        Logger.d(this, "FetchCards: PostParams: "+postParams);
//        final Intent baseActivityIntent = intent;
//        ApiManager.getPaymentApi().getPaymentCardHash(merchantKey)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<MerchantHashResponse>() {
//                    @Override
//                    public void onCompleted() {}
//
//                    @Override
//                    public void onError(Throwable e) {}
//
//                    @Override
//                    public void onNext(MerchantHashResponse merchantHashResponse) {
//                        HashMap<String, String> cardTokens = new HashMap<String, String>();
//                        for (MerchantHashResponse.Hash hash : merchantHashResponse.getMerchantHashes()) {
//                            cardTokens.put(hash.getCardToken(), hash.getMerchantHash());
//                        }
//
//                        baseActivityIntent.putExtra(PayuConstants.ONE_CLICK_CARD_TOKENS, cardTokens);
//                        startActivityForResult(baseActivityIntent, PayuConstants.PAYU_REQUEST_CODE);
//                    }
//                });
//    }
//
//    /**
//     * This method deletes merchantHash and cardToken from server side file.
//     *
//     * @param cardToken cardToken of card whose merchantHash and cardToken needs to be deleted from merchant server
//     */
//    private void deleteMerchantHash(String cardToken) {
//        ApiManager.getPaymentApi().deletePaymentCardHash(cardToken)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<StatusResponse>() {
//                    @Override
//                    public void onCompleted() {}
//
//                    @Override
//                    public void onError(Throwable e) {}
//
//                    @Override
//                    public void onNext(StatusResponse statusResponse) {}
//                });
//    }
//
//    /**
//     * This method prepares a HashMap of cardToken as key and merchantHash as value.
//     *
//     * @param merchantKey     merchant key used
//     * @param userCredentials unique credentials of the user usually of the form key:userId
//     */
//    public HashMap<String, String> getAllOneClickHashHelper(String merchantKey, String userCredentials) {
//        Logger.d(this, "getAllOneClickHashHelper");
//
//        return ApiManager.getPaymentApi().getPaymentCardHash(merchantKey)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(merchantHashResponse -> {
//                    HashMap<String, String> cardTokens = new HashMap<String, String>();
//                    for (MerchantHashResponse.Hash hash : merchantHashResponse.getMerchantHashes()) {
//                        Logger.d(this, "Stored Hash: "+hash.getCardToken()+", "+hash.getMerchantHash());
//                        cardTokens.put(hash.getCardToken(), hash.getMerchantHash());
//                    }
//                    return cardTokens;
//                })
//                .toBlocking().first();
//    }
//
//    /**
//     * Returns a HashMap object of cardToken and one click hash from merchant server.
//     * <p>
//     * This method will be called as a async task, regardless of merchant implementation.
//     * Hence, not to call this function as async task.
//     * The function should return a cardToken and corresponding one click hash as a hashMap.
//     *
//     * @param userCreds a string giving the user credentials of user.
//     * @return the Hash Map of cardToken and one Click hash.
//     **/
//    @Override
//    public HashMap<String, String> getAllOneClickHash(String userCreds) {
//        // 1. GET http request from your server
//        // GET params - merchant_key, user_credentials.
//        // 2. In response we get a
//        // this is a sample code for fetching one click hash from merchant server.
//        return getAllOneClickHashHelper(merchantKey, userCreds);
//    }
//
//    @Override
//    public void getOneClickHash(String cardToken, String merchantKey, String userCredentials) {}
//
//
//    /**
//     * This method will be called as a async task, regardless of merchant implementation.
//     * Hence, not to call this function as async task.
//     * This function save the oneClickHash corresponding to its cardToken
//     *
//     * @param cardToken    a string containing the card token
//     * @param oneClickHash a string containing the one click hash.
//     **/
//
//    @Override
//    public void saveOneClickHash(String cardToken, String oneClickHash) {
//        // 1. POST http request to your server
//        // POST params - merchant_key, user_credentials,card_token,merchant_hash.
//        // 2. In this POST method the oneclickhash is stored corresponding to card token in merchant server.
//        // this is a sample code for storing one click hash on merchant server.
//        storeMerchantHash(cardToken, oneClickHash);
//    }
//
//    /**
//     * This method will be called as a async task, regardless of merchant implementation.
//     * Hence, not to call this function as async task.
//     * This function delete’s the oneClickHash from the merchant server
//     *
//     * @param cardToken       a string containing the card token
//     * @param userCredentials a string containing the user credentials.
//     **/
//
//    @Override
//    public void deleteOneClickHash(String cardToken, String userCredentials) {
//        // 1. POST http request to your server
//        // POST params  - merchant_hash.
//        // 2. In this POST method the oneclickhash is deleted in merchant server.
//        // this is a sample code for deleting one click hash from merchant server.
//        deleteMerchantHash(cardToken);
//    }
//}