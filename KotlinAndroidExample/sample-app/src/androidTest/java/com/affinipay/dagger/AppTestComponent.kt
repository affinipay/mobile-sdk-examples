package com.affinipay.dagger

import com.affinipay.ChargeModel
import com.affinipay.LoginAccountModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(NetworkTestModule::class))
interface AppTestComponent : AppComponent {

  fun chargeModel(): ChargeModel

  fun loginAccountModel(): LoginAccountModel

}
