package com.affinipay

import com.google.gson.annotations.SerializedName

class Charge {

  @SerializedName("attributes") val chargeDetails: ChargeDetails? = null
  @SerializedName("receipt_html") val receiptHtml: String? = null

  override fun toString(): String {
    return chargeDetails.toString()
  }
}