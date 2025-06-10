package com.example.proyecto_rastreo_gps.ui

import com.google.gson.annotations.SerializedName

data class UbicacionModelRequest(
    @SerializedName("ru")
    var ru: Int,
    @SerializedName("codcli")
    var codcli: Int,
    @SerializedName("latitude")
    var latitude: Double,
    @SerializedName("longitude")
    var longitude: Double
)
