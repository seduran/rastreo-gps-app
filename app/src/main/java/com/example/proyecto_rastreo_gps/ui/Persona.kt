package com.example.proyecto_rastreo_gps.ui

import com.google.gson.annotations.SerializedName

data class Persona(
    @SerializedName("codcli")
    val codcli: Int,

    @SerializedName("cliente")
    val cliente: String
)
