package com.example.proyecto_rastreo_gps.ui

import com.google.gson.annotations.SerializedName

data class UbicacionModelResponse(
    @SerializedName("httpHeaders")
    val httpHeaders: Map<String, String>?,
    @SerializedName("httpStatusCode")
    val httpStatusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("otherParams")
    val otherParams: Map<String, Any>?,
    @SerializedName("data")
    val data: PersonaUbicacion?,
    @SerializedName("data2")
    val data2: Any?
)
