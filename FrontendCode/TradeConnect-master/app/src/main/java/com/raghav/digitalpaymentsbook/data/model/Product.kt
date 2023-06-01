package com.raghav.digitalpaymentsbook.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val productName: String,
    val productPrice: Int) : Parcelable