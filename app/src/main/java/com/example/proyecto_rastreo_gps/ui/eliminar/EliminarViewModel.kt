package com.example.proyecto_rastreo_gps.ui.eliminar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EliminarViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Eliminar Persona"
    }
    val text: LiveData<String> = _text
}