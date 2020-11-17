package com.affinipay

import com.affinipay.cardreadersdk.charge.PointOfSale
import com.google.gson.annotations.SerializedName

class ChargeRequestDetails {
  @SerializedName("pos") lateinit var pos: PointOfSale
}