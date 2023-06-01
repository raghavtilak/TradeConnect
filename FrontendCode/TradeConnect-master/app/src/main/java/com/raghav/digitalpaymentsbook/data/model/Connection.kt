package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Parcelize
data class Connection(
    @SerializedName("_id")
    val id: ObjectId,
    val isCreatedByUser: Boolean,
    val user: Retailer,
) : Parcelable
