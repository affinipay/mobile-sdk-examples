package com.affinipay

import com.google.gson.annotations.SerializedName

class ChargeDetails {

  @SerializedName("id") val id: String? = null
  @SerializedName("authorized_amount") val authorizedAmount: Int? = null
  @SerializedName("amount") val amount: Int? = null
  @SerializedName("created") val created: String? = null
  @SerializedName("completed_at") val completedAt: Any? = null
  @SerializedName("voided_at") val voidedAt: Any? = null
  @SerializedName("batched_at") val batchedAt: Any? = null
  @SerializedName("merchant_id") val merchantId: String? = null
  @SerializedName("account_id") val accountId: String? = null
  @SerializedName("recurring_charge_id") val recurringChargeId: String? = null
  @SerializedName("recurring_charge_occurrence_id") val recurringChargeOccurrenceId: String? = null
  @SerializedName("type") val type: String? = null
  @SerializedName("status") val status: String? = null
  @SerializedName("signature_token") val signatureToken: String? = null
  @SerializedName("signature_id") val signatureId: Any? = null
  @SerializedName("auto_capture") val autoCapture: Boolean = false
  @SerializedName("nonoriginating_refund_enabled") val nonoriginatingRefundEnabled: Boolean = false
  @SerializedName("currency") val currency: String? = null
  @SerializedName("authorization_code") val authorizationCode: String? = null
  @SerializedName("avs_result") val avsResult: String? = null
  @SerializedName("cvv_result") val cvvResult: String? = null
  @SerializedName("payment_data_source") val paymentDataSource: String? = null
  @SerializedName("reference") val reference: Any? = null
  @SerializedName("void_reference") val voidReference: Any? = null
  @SerializedName("capture_reference") val captureReference: Any? = null
  @SerializedName("ip_address") val ipAddress: String? = null
  @SerializedName("name") val name: Any? = null
  @SerializedName("address1") val address1: String? = null
  @SerializedName("address2") val address2: Any? = null
  @SerializedName("city") val city: Any? = null
  @SerializedName("state") val state: Any? = null
  @SerializedName("postal_code") val postalCode: String? = null
  @SerializedName("country") val country: Any? = null
  @SerializedName("fingerprint") val fingerprint: String? = null
  @SerializedName("email") val email: Any? = null
  @SerializedName("phone") val phone: Any? = null
  @SerializedName("payment_page_id") val paymentPageId: Any? = null
  @SerializedName("data") val data: Any? = null
  @SerializedName("failure_code") val failureCode: Any? = null
  @SerializedName("failure_details") val failureDetails: Any? = null
  @SerializedName("application_id") val applicationId: Any? = null
  @SerializedName("geoip_location") val geoipLocation: Any? = null
  @SerializedName("method") val method: Any? = null
  @SerializedName("user_id") val userId: Int? = null

  companion object {

    val PAYMENT_SOURCE_CHIP = "CHIP"
  }

  override fun toString(): String {
    return "ID: $id\n Authorized Amount: $amount\n Created At: $created\n Status: $status\n Authorization Code: $authorizationCode"
  }
}