package com.example.proyecto_rastreo_gps.ui

import com.google.gson.annotations.SerializedName

data class PersonaModelRequest(
    @SerializedName("ru")
    var ru: Int,
    @SerializedName("name")
    var name: String,
    @SerializedName("first_name")
    var first_name: String,
    @SerializedName("last_name")
    var last_name: String
)
