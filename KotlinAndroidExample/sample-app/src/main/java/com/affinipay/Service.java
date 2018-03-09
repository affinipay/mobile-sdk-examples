package com.affinipay;

import com.affinipay.cardreadersdk.signature.SignatureRequest;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Service {

  @GET("api/v1/merchant") Observable<Accounts> getAccounts(@Header("X_AFP_APPLICATION_ID") String applicationId,
      @Header("X_AFP_CLIENT_KEY") String clientKey);

  @Headers("Content-Type: application/json")
  @POST("api/v1/charge") Observable<Charge> postCharge(@Header("X_AFP_APPLICATION_ID") String applicationId,
      @Header("X_AFP_CLIENT_KEY") String clientKey, @Header("X_AFP_PUBLIC_KEY") String publicKey, @Body ChargeRequest chargeRequest);

  @Headers("Content-Type: application/json")
  @POST("api/v1/sign") Observable<SignatureReceipt> postSignature(@Header("X_AFP_APPLICATION_ID") String applicationId,
      @Header("X_AFP_CLIENT_KEY") String clientKey, @Header("X_AFP_PUBLIC_KEY") String publicKey, @Body SignatureRequest signatureRequest);

}
