package com.raghav.digitalpaymentsbook.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class PreferenceManager {
    companion object : SingletonHolder<SharedPreferences,Context>({
        it.getSharedPreferences(
            Constants.SHARED_PREF_NAME,
            AppCompatActivity.MODE_PRIVATE
        )
    })
}