package com.example.proyecto_rastreo_gps.ui.listar

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto_rastreo_gps.databinding.FragmentListarBinding
import com.example.proyecto_rastreo_gps.ui.PostApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ListarFragment : Fragment() {
    private var _binding: FragmentListarBinding? = null
    private val binding get() = _binding!!

    private lateinit var personaAdapter: PersonaAdapter

    // Se usa 'by lazy' para que se cree solo una vez cuando se acceda por primera vez.
    private val apiService: PostApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://tallerweb.uajms.edu.bo/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(PostApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Configurar el RecyclerView (sin cambios)
        setupRecyclerView()

        // 3. Configurar el listener del botón (sin cambios)
        binding.button.setOnClickListener {
            val ru = binding.editTextText1.text.toString().trim()
            if (ru.isNotEmpty()) {
                fetchData(ru)
            } else {
                Toast.makeText(context, "Por favor, ingrese un RU", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        personaAdapter = PersonaAdapter(emptyList())
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = personaAdapter
        }
    }

    private fun fetchData(ru: String) {
        lifecycleScope.launch {
            try {
                // 4. Usamos la instancia local 'apiService' para hacer la llamada
                val response = apiService.getListaClientes(ru)

                if (response.isSuccessful) {
                    val personasList = response.body()?.data
                    if (personasList != null) {
                        personaAdapter.updateData(personasList)
                    } else {
                        Toast.makeText(context, "No se encontraron datos.", Toast.LENGTH_SHORT).show()
                        personaAdapter.updateData(emptyList()) // Limpiar la lista si no hay datos
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.code()} ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("ListarFragment", "Error al obtener datos", e)
                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}