package com.example.proyecto_rastreo_gps.ui.modificar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ModificarViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Modificar Persona"
    }
    val text: LiveData<String> = _text
}