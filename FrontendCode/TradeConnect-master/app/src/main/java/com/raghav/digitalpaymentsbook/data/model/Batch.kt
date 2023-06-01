package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId
import java.util.*

@Parcelize
data class Batch(

    @SerializedName("_id")
    val id: ObjectId?,

    val batchNo: String,
    val MRP: Int,
    val mfg: Date?,
    val expiry: Date?,
    val productName: String,
    val quantity: Int,
    val buyingPrice: Int?,
    val sellingPrice: Int,
    val isUpdateAllowed: Boolean?
) : Parcelable
