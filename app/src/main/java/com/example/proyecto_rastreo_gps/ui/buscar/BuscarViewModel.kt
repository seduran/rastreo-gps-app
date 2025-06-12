package com.example.proyecto_rastreo_gps.ui.buscar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BuscarViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Buscar Persona"
    }
    val text: LiveData<String> = _text
}