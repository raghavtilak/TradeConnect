package com.raghav.digitalpaymentsbook.data.model.apis

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Parcelize

data class CreateConnection (
    val recipient : ObjectId,
    val sourceType: String
): Parcelable
