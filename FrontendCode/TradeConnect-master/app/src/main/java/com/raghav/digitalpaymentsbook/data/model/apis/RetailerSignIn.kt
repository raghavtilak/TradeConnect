package com.raghav.digitalpaymentsbook.data.model.apis

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RetailerSignIn(
    val email:String?,
    val phone:String?
):Parcelable
