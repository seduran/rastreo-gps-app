package com.example.proyecto_rastreo_gps.ui.registrarUbicacion

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_rastreo_gps.databinding.FragmentRegistrarUbicacionBinding
import com.example.proyecto_rastreo_gps.ui.PostApiService
import com.example.proyecto_rastreo_gps.ui.UbicacionModelRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegistrarUbicacionFragment : Fragment() {

    private var _binding: FragmentRegistrarUbicacionBinding? = null
    private val binding get() = _binding!!

    // Declara el cliente de ubicación en el fragmento
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Código de solicitud de permisos
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private lateinit var service: PostApiService

    companion object {
        fun newInstance() = RegistrarUbicacionFragment()
    }

    private val viewModel: RegistrarUbicacionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializa FusedLocationProviderClient aquí, usando el contexto de la actividad
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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
            requestLocationPermissions()

        }
    }

    private fun requestLocationPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permissionsToRequest = ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    // Esta función se llama después de que el usuario responde al diálogo de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS_REQUEST_CODE -> {
                // Verifica si el permiso de ubicación principal fue concedido
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation() // Permiso concedido, ahora obtiene la ubicación
                } else {
                    Toast.makeText(requireContext(), "Permiso de ubicación denegado. No se puede obtener su ubicación.", Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Para otros requestCodes
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location -> // 'location' es de tipo Location? (nullable)
                    if (location != null) {
                        // Latitud y Longitud obtenidas
                        val latitude = location.latitude
                        val longitude = location.longitude

                        // *** ¡Aquí actualizamos los EditText! ***
                        binding.editTextText3.setText(latitude.toString())
                        binding.editTextText4.setText(longitude.toString())

                        Toast.makeText(requireContext(), "Ubicación obtenida: Lat $latitude, Lon $longitude", Toast.LENGTH_LONG).show()
                        validateAndSendRequest()
                    } else {
                        Toast.makeText(requireContext(), "No se pudo obtener la última ubicación conocida. Asegúrese de que la ubicación esté activada en el dispositivo.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al obtener la ubicación: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // Esto no debería ocurrir si requestLocationPermissions() se llamó y se manejó correctamente.
            Toast.makeText(requireContext(), "Permisos de ubicación no concedidos en la verificación final.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndSendRequest(){
        val ruString = binding.editTextText1.text.toString()
        val codCliString = binding.editTextText2.text.toString()
        val latitudeString = binding.editTextText3.text.toString()
        val longitudeString = binding.editTextText4.text.toString()

        if (ruString.isBlank() || codCliString.isBlank() || latitudeString.isBlank() || longitudeString.isBlank()) {
            Toast.makeText(requireContext(), "No debe haber campos vacíos", Toast.LENGTH_SHORT).show()
            return
        }
        val ru = ruString.toInt()
        val codCli = codCliString.toInt()
        val latitude = latitudeString.toDouble()
        val longitude = longitudeString.toDouble()

        val postRequest = UbicacionModelRequest(ru = ru, codcli = codCli, latitude = latitude, longitude = longitude)
        sendPostRequest(postRequest)
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