package com.affinipay.dagger

import com.affinipay.App
import com.affinipay.ContainerActivity
import com.affinipay.LoginAccountView
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(NetworkModule::class))
interface AppComponent {

  fun inject(application: App)

  fun inject(target: ContainerActivity)

  fun inject(target: LoginAccountView)
}
