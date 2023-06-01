package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Parcelize
data class Customer(
    val name: String,
    val email: String,
    val phone: String,
    val address: String?,
    val registrationToken: String?,
    @SerializedName("_id")
    val id: ObjectId?
) : Parcelable
