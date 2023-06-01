package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Parcelize
data class User(
    val name: String,
    val email: String,
    val registrationToken: String?,
    val password: String?,
    val phone: String,
    val address: String,
    val role: UserRole,
    val businessName: String?,
    val businessType:String?,
    val totalSales:Int,
    @SerializedName("_id")
    val id: ObjectId


//    "_id": "640eb46fb178e41b37d1e51d",
//"businessName": "Kirana",
//"name": "Mukul",
//"phone": "9610373175",
//"role": "Retailer",
//"address": "IIT jagatpura",
//"createdAt": "2023-03-13T05:28:15.040Z",
//"__v": 0
):Parcelable