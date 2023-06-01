package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.raghav.digitalpaymentsbook.data.model.enums.TransactionStatus
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId
import java.util.*

@Parcelize
data class Transaction(
    val createdAt: Date,
    val status: TransactionStatus,
    val amount: Int,
    val paid: Int,
    val due: Int,
    val retailer: ObjectId,
    val customerName: String,
    val customerPhone: Long,
    val note: String,
    val products: List<Product>,
    val transactionName:String,
    @SerializedName("_id")
    val id: ObjectId?
) : Parcelable