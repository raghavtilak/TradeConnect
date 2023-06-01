package com.raghav.digitalpaymentsbook.data.model

import org.bson.types.ObjectId

data class StoreItem(
    val batchIds: List<ObjectId>,
    val sku: String,
    val productName: String,
    val quantity: Int
)
