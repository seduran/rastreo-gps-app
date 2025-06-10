package com.example.proyecto_rastreo_gps.ui.registrar

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
import com.example.proyecto_rastreo_gps.databinding.FragmentRegistrarBinding
import com.example.proyecto_rastreo_gps.ui.PostApiService
import com.example.proyecto_rastreo_gps.ui.PersonaModelRequest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegistrarFragment : Fragment() {

    private var _binding: FragmentRegistrarBinding? = null
    private val binding get() = _binding!!

    private lateinit var service: PostApiService

    companion object {
        fun newInstance() = RegistrarFragment()
    }

    private val viewModel: RegistrarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val registrarViewModel =
            ViewModelProvider(this).get(RegistrarViewModel::class.java)

        _binding = FragmentRegistrarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textRegistrar

        registrarViewModel.text.observe(viewLifecycleOwner){
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
            val name = binding.editTextText2.text.toString()
            val first_name = binding.editTextText3.text.toString()
            val last_name = binding.editTextText4.text.toString()

            if (ruString.isBlank() || name.isBlank() || first_name.isBlank() || last_name.isBlank()) {
                Toast.makeText(requireContext(), "No debe haber campos vacíos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val ru = ruString.toInt()

            val postRequest = PersonaModelRequest(ru = ru, name = name, first_name = first_name, last_name = last_name)
            sendPostRequest(postRequest)
        }
    }

    private fun sendPostRequest(postData: PersonaModelRequest) {
        viewLifecycleOwner.lifecycleScope.launch {
            clearTextViews()
            val response = service.addDatos(postData)
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