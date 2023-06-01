package com.raghav.digitalpaymentsbook.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.flow.first
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor to add auth token to requests
 */
class AuthInterceptor(val context: Context) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        val token = PreferenceManager.getInstance(context).getAuthToken()

        val requestBuilder = chain.request().newBuilder()

        // If token has been saved, add it to the request
        requestBuilder.addHeader("Authorization", "Bearer $token")

        return chain.proceed(requestBuilder.build())
    }
}