package com.example.proyecto_rastreo_gps.ui

import com.google.gson.annotations.SerializedName

data class PersonaModelResponse(
    @SerializedName("httpHeaders")
    val httpHeaders: Map<String, String>?,
    @SerializedName("httpStatusCode")
    val httpStatusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("otherParams")
    val otherParams: Map<String, Any>?,
    @SerializedName("data")
    val data: Boolean?,
    @SerializedName("data2")
    val data2: Any?
)
