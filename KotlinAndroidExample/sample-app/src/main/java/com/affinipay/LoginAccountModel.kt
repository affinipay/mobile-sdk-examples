package com.affinipay

import io.reactivex.Observable

open class LoginAccountModel(val service: Service) {

  open fun getAccounts() : Observable<Accounts> {
    return service.getAccounts("000", "000")
  }

}