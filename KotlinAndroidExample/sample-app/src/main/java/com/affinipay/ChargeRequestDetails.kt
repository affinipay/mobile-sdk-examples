package com.affinipay

import com.affinipay.cardreadersdk.charge.PointOfSale
import com.google.gson.annotations.SerializedName

class ChargeRequestDetails {
  @SerializedName("amount") lateinit var amount: String
  @SerializedName("pos") lateinit var pos: PointOfSale
}