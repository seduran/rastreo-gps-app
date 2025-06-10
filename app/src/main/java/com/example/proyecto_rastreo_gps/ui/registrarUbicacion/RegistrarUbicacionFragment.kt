package com.example.proyecto_rastreo_gps.ui.registrarUbicacion

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_rastreo_gps.databinding.FragmentRegistrarUbicacionBinding
import com.example.proyecto_rastreo_gps.ui.PostApiService
import com.example.proyecto_rastreo_gps.ui.UbicacionModelRequest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegistrarUbicacionFragment : Fragment() {

    private var _binding: FragmentRegistrarUbicacionBinding? = null
    private val binding get() = _binding!!

    private lateinit var service: PostApiService

    companion object {
        fun newInstance() = RegistrarUbicacionFragment()
    }

    private val viewModel: RegistrarUbicacionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val registrarUbicacionViewModel =
            ViewModelProvider(this).get(RegistrarUbicacionViewModel::class.java)

        _binding = FragmentRegistrarUbicacionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textRegistrar

        registrarUbicacionViewModel.text.observe(viewLifecycleOwner){
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://tallerweb.uajms.edu.bo/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(PostApiService::class.java)

        binding.button.setOnClickListener {
            val ruString = binding.editTextText1.text.toString()
            val codCliString = binding.editTextText2.text.toString()
            val latitudeString = binding.editTextText3.text.toString()
            val longitudeString = binding.editTextText4.text.toString()

            if (ruString.isBlank() || codCliString.isBlank() || latitudeString.isBlank() || longitudeString.isBlank()) {
                Toast.makeText(requireContext(), "No debe haber campos vacíos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val ru = ruString.toInt()
            val codCli = codCliString.toInt()
            val latitude = latitudeString.toDouble()
            val longitude = longitudeString.toDouble()

            val postRequest = UbicacionModelRequest(ru = ru, codcli = codCli, latitude = latitude, longitude = longitude)
            sendPostRequest(postRequest)
        }
    }

    private fun sendPostRequest(postData: UbicacionModelRequest) {
        viewLifecycleOwner.lifecycleScope.launch {
            clearTextViews()
            val response = service.addDatosUbicacion(postData)
            if (response.isSuccessful) {
                val postResponse = response.body()
                postResponse?.let {
                    binding.textView5.setText("${it.message}")
                } ?: run {
                    Log.e("Fragment_1_POST", "Respuesta nula del servidor")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("Error de retrofit Fragment_1", "Error: ${response.code()} - ${response.message()} - $errorBody")
                binding.textView5.setText("Error: ${response.code()}")
            }
        }
    }
    private fun clearTextViews() {
        binding.textView5.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}