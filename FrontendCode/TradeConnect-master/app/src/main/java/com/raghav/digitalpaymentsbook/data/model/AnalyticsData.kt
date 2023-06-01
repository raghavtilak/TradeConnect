package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId
import java.util.*

@Parcelize
data class AnalyticsData(
    val key: Int,
    val count: Int,
) : Parcelable
