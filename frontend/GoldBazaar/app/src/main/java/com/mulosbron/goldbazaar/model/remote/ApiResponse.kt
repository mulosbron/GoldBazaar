package com.mulosbron.goldbazaar.model.remote

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,     // may be null
    val data: T
)
