package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.raghav.digitalpaymentsbook.data.model.enums.OrderStatus
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId
import java.util.*

@Parcelize
data class Order(

    @SerializedName("_id")
    val id: ObjectId,
    val isCreatedByUser : Boolean,
    val status: OrderStatus,
    val createdAt: Date,
    val total: Int,
    val batches: List<Batch>,
    val user: Retailer

) : Parcelable