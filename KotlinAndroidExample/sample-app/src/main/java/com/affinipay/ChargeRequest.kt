package com.affinipay

import com.google.gson.annotations.SerializedName

class ChargeRequest {

  @SerializedName("amount") lateinit var amount: String
  @SerializedName("account_id") lateinit var accountId: String
  @SerializedName("charge") lateinit var chargeRequestDetails: ChargeRequestDetails
  @SerializedName("token_id") lateinit var tokenId: String
  @SerializedName("source_id") lateinit var sourceId: String

}