package com.raghav.digitalpaymentsbook.data.model.enums

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Parcelize
data class BusinessTypes(
    val name: String,
    val description: String,
    @SerializedName("_id")
    val id: ObjectId
) : Parcelable {
    override fun toString(): String {
        return name
    }
}
