package com.raghav.digitalpaymentsbook.data.model.apis

import com.raghav.digitalpaymentsbook.data.model.Connection

data class ServerResponse(
    val message:String?,
    val error:String?,
    val connection: Connection?
)
