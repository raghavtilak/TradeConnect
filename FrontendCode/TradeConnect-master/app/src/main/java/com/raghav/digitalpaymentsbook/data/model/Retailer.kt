package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Parcelize
data class Retailer(
    val name: String,
    val email: String,
    val password: String?,
    val phone: String,
    val address: String,
    val businessName: String,
    val businessType:String,
    val totalSales:Int,
    val registrationToken: String?,
    @SerializedName("_id")
    val id: ObjectId
) : Parcelable
