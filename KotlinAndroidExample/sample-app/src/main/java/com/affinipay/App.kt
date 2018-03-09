package com.affinipay

import android.content.Context
import android.support.multidex.MultiDexApplication
import com.affinipay.dagger.AppComponent
import com.affinipay.dagger.DaggerAppComponent
import com.affinipay.dagger.NetworkModule

open class App : MultiDexApplication() {

  lateinit var component: AppComponent

  override fun onCreate() {
    super.onCreate()
    component = DaggerAppComponent.builder().networkModule(NetworkModule(this)).build()
  }

  companion object {
    operator fun get(context: Context): App {
      return context.applicationContext as App
    }
  }

}
