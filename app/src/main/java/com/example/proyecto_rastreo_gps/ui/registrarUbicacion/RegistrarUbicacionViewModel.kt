package com.example.proyecto_rastreo_gps.ui.registrarUbicacion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistrarUbicacionViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Registrar Ubicación"
    }
    val text: LiveData<String> = _text
}