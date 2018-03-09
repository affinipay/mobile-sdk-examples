package com.affinipay

import com.google.gson.annotations.SerializedName

class SignatureReceipt {

  @SerializedName("attributes") val chargeDetails: ChargeDetails? = null

  override fun toString(): String {
    return chargeDetails.toString()
  }
}