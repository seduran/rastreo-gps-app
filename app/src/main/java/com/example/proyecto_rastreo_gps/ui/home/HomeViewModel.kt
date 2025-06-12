package com.example.proyecto_rastreo_gps.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "¡Bienvenido/a a la aplicación! Por favor, seleccione una opción del menú."
    }
    val text: LiveData<String> = _text
}