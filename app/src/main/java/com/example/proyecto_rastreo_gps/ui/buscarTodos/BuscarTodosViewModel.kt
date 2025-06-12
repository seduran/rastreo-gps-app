package com.example.proyecto_rastreo_gps.ui.buscarTodos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BuscarTodosViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Ubicación Personas"
    }
    val text: LiveData<String> = _text
}