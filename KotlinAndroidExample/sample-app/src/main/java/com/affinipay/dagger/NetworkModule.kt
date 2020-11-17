package com.affinipay.dagger

import androidx.multidex.MultiDexApplication
import com.affinipay.BuildConfig
import com.affinipay.ChargeModel
import com.affinipay.LoginAccountModel
import com.affinipay.Service
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule(val app: MultiDexApplication) {

  @Provides
  @Singleton
  fun provideRetrofit(): Retrofit {
    val builder = OkHttpClient.Builder()

    val logging = HttpLoggingInterceptor()

    if (BuildConfig.DEBUG) {
      logging.level = HttpLoggingInterceptor.Level.BODY
    } else {
      logging.level = HttpLoggingInterceptor.Level.BASIC
    }

    val httpClient = builder.readTimeout(30, TimeUnit.SECONDS).connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging).build()

    return Retrofit.Builder().baseUrl(BuildConfig.LOCAL_SERVER)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
  }

  @Provides
  @Singleton
  fun provideService(retrofit: Retrofit): Service {
    return retrofit.create(Service::class.java)
  }

  @Provides
  @Singleton
  fun provideAccountModel(service: Service): LoginAccountModel {
    return LoginAccountModel(service)
  }

  @Provides
  @Singleton
  fun provideChargeModel(service: Service): ChargeModel {
    return ChargeModel(service)
  }
}
