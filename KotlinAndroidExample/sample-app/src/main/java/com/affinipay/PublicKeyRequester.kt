package com.affinipay

interface PublicKeyRequester {

  fun onAccountChosen(publicKey: String, accountId: String)

}