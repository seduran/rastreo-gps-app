package com.example.proyecto_rastreo_gps.ui.eliminar

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
import com.example.proyecto_rastreo_gps.databinding.FragmentEliminarBinding
import com.example.proyecto_rastreo_gps.ui.PostApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EliminarFragment : Fragment() {

    private var _binding: FragmentEliminarBinding? = null
    private val binding get() = _binding!!

    private lateinit var service: PostApiService

    companion object {
        fun newInstance() = EliminarFragment()
    }

    private val viewModel: EliminarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val eliminarViewModel =
            ViewModelProvider(this).get(EliminarViewModel::class.java)

        _binding = FragmentEliminarBinding.inflate(inflater,container,false)
        val root: View = binding.root

        val textView: TextView = binding.textEliminar

        eliminarViewModel.text.observe(viewLifecycleOwner){
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
            val ru = binding.editTextText1.text.toString()
            val codCli = binding.editTextText2.text.toString()

            if (ru.isBlank() || codCli.isBlank()) {
                Toast.makeText(requireContext(), "No debe haber campos vacíos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendDeleteRequest(codCli, ru)
        }
    }

    private fun sendDeleteRequest(codcli: String, ru: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val response = service.deletePost(codcli, ru)
            if (response.isSuccessful) {
                val postResponse = response.body()
                postResponse?.let {
                    binding.textView3.setText("Message: ${it.message}")
                } ?: run {
                    Log.e("Fragment_3_DELETE", "Respuesta nula del servidor")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(
                    "Error de retrofit Fragment_3",
                    "Error: ${response.code()} - ${response.message()} - $errorBody"
                )
                binding.textView3.setText("Error: ${response.code()}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}