package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Parcelize
data class MyProfile(
    val name: String,
    val email: String,
    val registrationToken: String?,
    val password: String?,
    val phone: String,
    val address: String,
    val role: UserRole,
    val businessName: String?,
    val businessType: String?,
    val totalSales: Int,
    val totalConnections: Int,
    val createdOrders: Int,
    val receivedOrders: Int,

    @SerializedName("_id")
    val id: ObjectId
) : Parcelable