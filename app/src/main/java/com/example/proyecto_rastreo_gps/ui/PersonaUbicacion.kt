package com.example.proyecto_rastreo_gps.ui

import com.google.gson.annotations.SerializedName

data class PersonaUbicacion(
    @SerializedName("codcli")
    val codcli: Int,
    @SerializedName("cliente")
    val cliente: String,
    @SerializedName("latitude")
    var latitude: Double,
    @SerializedName("longitude")
    var longitude: Double
)
