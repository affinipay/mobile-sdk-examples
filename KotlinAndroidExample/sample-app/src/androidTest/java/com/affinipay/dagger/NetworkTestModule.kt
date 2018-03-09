package com.affinipay.dagger

import android.app.Application
import com.affinipay.ChargeModel
import com.affinipay.LoginAccountModel
import dagger.Module
import dagger.Provides
import org.mockito.Mockito.mock
import javax.inject.Singleton


@Module
class NetworkTestModule(private val mApplication: Application) {

//    @Provides
//    @Singleton
//    fun provideRetrofit(): Retrofit {
//        val builder = OkHttpClient.Builder()
//
//        val logging = HttpLoggingInterceptor()
//
//        if (BuildConfig.DEBUG) {
//            logging.level = HttpLoggingInterceptor.Level.BODY
//        } else {
//            logging.level = HttpLoggingInterceptor.Level.BASIC
//        }
//
//        val httpClient = builder.readTimeout(30, TimeUnit.SECONDS).connectTimeout(30, TimeUnit.SECONDS)
//            .addInterceptor(logging).build()
//
//        return Retrofit.Builder().baseUrl("http://10.1.188.127:9292/")
//            .client(httpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideService(retrofit: Retrofit): Service {
//        return retrofit.create(Service::class.java)
//    }



    @Provides
    @Singleton
    fun provideAccountModel(): LoginAccountModel {
        return mock(LoginAccountModel::class.java)
    }


    @Provides
    @Singleton
    fun provideChargeModel(): ChargeModel {
        return mock(ChargeModel::class.java)
    }
}