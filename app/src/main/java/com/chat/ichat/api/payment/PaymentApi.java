package com.chat.ichat.api.payment;

import com.chat.ichat.api.StatusResponse;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by vidhun on 13/07/17.
 */

public interface PaymentApi {
    @GET("payment/hash/{merchant_key}")
    Observable<MerchantHashResponse> getPaymentCardHash(@Path("merchant_key") String merchantKey);

    @GET("payment/hash/id/{id}")
    Observable<MerchantHashResponse> getPaymentCardHashById(@Path("id") String id);

    @POST("payment/hash")
    Observable<MerchantHashResponse> storePaymentCardHash(@Body MerchantHashRequest merchantHashRequest);

    @PUT("payment/hash")
    Observable<MerchantHashResponse> updarePaymentCardHash(@Body MerchantHashRequest merchantHashRequest);

    @DELETE("payment/hash/{card_token}")
    Observable<StatusResponse> deletePaymentCardHash(@Path("card_token") String cardToken);

    @POST("payment/get_hash")
    Observable<PaymentsHashResponse> getPaymentsHash(@Body PaymentsDetailsRequest paymentsDetailsRequest);
}
