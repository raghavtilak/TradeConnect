package com.raghav.digitalpaymentsbook.data.model.apis

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.raghav.digitalpaymentsbook.data.model.Batch
import com.raghav.digitalpaymentsbook.data.model.Retailer
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId
import java.util.*

@Parcelize
data class RetailerProduct(
    @SerializedName("_id")
    val id: ObjectId,
    val productName: String,
    val batches: List<Batch>
) : Parcelable
