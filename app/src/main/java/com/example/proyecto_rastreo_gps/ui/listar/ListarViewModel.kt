package com.example.proyecto_rastreo_gps.ui.listar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListarViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Listar Personas"
    }
    val text: LiveData<String> = _text
}