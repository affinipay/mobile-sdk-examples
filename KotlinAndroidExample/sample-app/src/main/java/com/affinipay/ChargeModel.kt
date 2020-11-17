package com.affinipay

import com.affinipay.cardreadersdk.charge.PointOfSale
import io.reactivex.Observable
import java.util.*

open class ChargeModel(private val service: Service) {

  open fun makeCharge(publicKey: String, accountId: String, oneTimeToken: String, amount: String) : Observable<Charge> {

    val chargeRequest = ChargeRequest()
    chargeRequest.amount = amount
    chargeRequest.accountId = accountId
    chargeRequest.tokenId = oneTimeToken
    chargeRequest.sourceId = "AffinipaySampleAndroid:" + UUID.randomUUID()

    return service.postCharge("000", "000", publicKey, chargeRequest)
  }

  open fun makeEMVCharge(publicKey: String, accountId: String, oneTimeToken: String, amount: String, paymentDataSource: String?,
      pos: PointOfSale) : Observable<Charge> {
    val details = ChargeRequestDetails()
    details.pos = pos
    val chargeRequest = ChargeRequest()
    chargeRequest.amount = amount
    chargeRequest.chargeRequestDetails = details
    chargeRequest.accountId = accountId
    chargeRequest.tokenId = oneTimeToken
    chargeRequest.sourceId = "AffinipaySampleAndroid:" + UUID.randomUUID()

    return service.postCharge("000", "000", publicKey, chargeRequest)
  }

}