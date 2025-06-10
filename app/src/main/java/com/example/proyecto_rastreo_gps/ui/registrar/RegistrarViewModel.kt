package com.example.proyecto_rastreo_gps.ui.registrar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistrarViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Registrar Persona"
    }
    val text: LiveData<String> = _text
}