package com.example.proyecto_rastreo_gps.ui.buscar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.proyecto_rastreo_gps.R
import com.example.proyecto_rastreo_gps.ui.PostApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BuscarFragment : Fragment() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var map : MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    private lateinit var service: PostApiService
    private var searchMarker: Marker? = null // Para mantener una referencia al marcador de búsqueda

    // onCreate es para inicializaciones no relacionadas con la vista
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Inicializar Retrofit y el servicio de la API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://tallerweb.uajms.edu.bo/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(PostApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val buscarViewModel =
            ViewModelProvider(this).get(BuscarViewModel::class.java)

        val rootView = inflater.inflate(R.layout.fragment_buscar, container, false)

        val textView: TextView = rootView.findViewById(R.id.text_buscar)
        buscarViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        map = rootView.findViewById(R.id.map)
        val codCliEditText: EditText = rootView.findViewById(R.id.editTextText2)
        val buscarButton: Button = rootView.findViewById(R.id.button)

        // Configuración inicial del mapa
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(requireContext()))
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        map.isHorizontalMapRepetitionEnabled = false
        map.isVerticalMapRepetitionEnabled = false

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        map.overlays.add(myLocationOverlay)

        // Lógica para el botón de búsqueda
        buscarButton.setOnClickListener {
            val codCli = codCliEditText.text.toString().trim()
            if (codCli.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, ingrese un código de cliente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            buscarUbicacionCliente(codCli)
        }

        return rootView
    }

    private fun buscarUbicacionCliente(codCli: String) {
        // Usar el scope de corrutinas del ciclo de vida del fragmento
        lifecycleScope.launch {
            try {
                val response = service.getUbicacionPersona(codCli)
                if (response.isSuccessful) {
                    val ubicacionResponse = response.body()
                    val personaUbicacion = ubicacionResponse?.data

                    if (personaUbicacion != null) {
                        // Eliminar el marcador anterior si existe
                        searchMarker?.let { map.overlays.remove(it) }

                        val locationPoint = GeoPoint(personaUbicacion.latitude, personaUbicacion.longitude)

                        // Crear y configurar el nuevo marcador
                        val newMarker = Marker(map)
                        newMarker.position = locationPoint
                        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        newMarker.title = "Codcli: ${personaUbicacion.codcli}" // Título con el código del cliente
                        newMarker.snippet = personaUbicacion.cliente       // Snippet con el nombre del cliente

                        val personIcon = ContextCompat.getDrawable(requireContext(), R.drawable.outline_accessibility_new_24)
                        newMarker.icon = personIcon

                        map.overlays.add(newMarker)
                        searchMarker = newMarker // Guardar referencia al nuevo marcador

                        // Centrar y hacer zoom en la nueva ubicación
                        map.controller.animateTo(locationPoint)
                        map.controller.setZoom(17.0)
                        map.invalidate() // Refrescar el mapa

                    } else {
                        Toast.makeText(requireContext(), "Cliente no encontrado o sin datos de ubicación.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta del servidor: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestLocationPermissions() // Llama a la solicitud de permisos aquí
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(requireContext()))
        map.onResume()
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
    }

    override fun onPause() {
        super.onPause()
        Configuration.getInstance().save(context, PreferenceManager.getDefaultSharedPreferences(requireContext()))
        map.onPause()
        myLocationOverlay.disableMyLocation()
        myLocationOverlay.disableFollowLocation()
    }

    private fun requestLocationPermissions() {
        val permissionsToRequest = ArrayList<String>()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        } else {
            getAndCenterMapOnInitialLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS_REQUEST_CODE -> {
                val locationGranted = grantResults.isNotEmpty() &&
                        (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                                (permissions.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))

                if (locationGranted) {
                    getAndCenterMapOnInitialLocation()
                } else {
                    Toast.makeText(requireContext(), "Permiso de ubicación denegado. No se puede mostrar su ubicación inicial.", Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun getAndCenterMapOnInitialLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { receivedLocation ->
                    if (receivedLocation != null) {
                        val latitude = receivedLocation.latitude
                        val longitude = receivedLocation.longitude
                        Toast.makeText(requireContext(), "Ubicación inicial: Lat $latitude, Lon $longitude", Toast.LENGTH_LONG).show()

                        val userLocation = GeoPoint(latitude, longitude)
                        map.controller.setCenter(userLocation)
                        map.controller.setZoom(16.0)
                        map.invalidate()
                    } else {
                        Toast.makeText(requireContext(), "No se pudo obtener la última ubicación conocida.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al obtener la ubicación inicial: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(requireContext(), "Permisos de ubicación no concedidos para obtener ubicación inicial.", Toast.LENGTH_SHORT).show()
        }
    }
}