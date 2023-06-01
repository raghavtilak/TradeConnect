package com.raghav.digitalpaymentsbook.data.network

import android.content.Context
import android.util.Log
import com.raghav.digitalpaymentsbook.util.Constants
import com.raghav.digitalpaymentsbook.util.GsonUtils
import com.raghav.digitalpaymentsbook.util.SingletonHolder
import com.raghav.digitalpaymentsbook.util.UnsafeOkHttpClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Client
class RetrofitHelper {


//    private fun retrofitService(): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(UnsafeOkHttpClient().getUnsafeOkHttpClient())
//            .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson))
//            .build()
//    }

    companion object : SingletonHolder<UserAPI,Context>({
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(UnsafeOkHttpClient(it).getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson))
            .build().create(UserAPI::class.java)
    })

//    val userAPI: UserAPI by lazy {
//        retrofitService().create(UserAPI::class.java)
//    }

}