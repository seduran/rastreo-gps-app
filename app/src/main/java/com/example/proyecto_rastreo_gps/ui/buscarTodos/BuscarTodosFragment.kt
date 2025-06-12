package com.example.proyecto_rastreo_gps.ui.buscarTodos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.proyecto_rastreo_gps.R
import com.example.proyecto_rastreo_gps.ui.PostApiService
import com.example.proyecto_rastreo_gps.ui.PersonaUbicacion
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

class BuscarTodosFragment : Fragment() {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var map : MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    private lateinit var service: PostApiService
    private var clientMarkers = mutableListOf<Marker>() // Lista para guardar los marcadores

    // onCreate es para inicializaciones no relacionadas con la vista
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Inicializar Retrofit y el servicio de la API
        val retrofit = Retrofit.Builder()
            // !! IMPORTANTE !! Reemplace con la URL base de su API. Debe terminar en /
            .baseUrl("https://tallerweb.uajms.edu.bo/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(PostApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val buscarTodosViewModel =
            ViewModelProvider(this).get(BuscarTodosViewModel::class.java)

        val rootView = inflater.inflate(R.layout.fragment_buscar_todos, container, false)

        val textView: TextView = rootView.findViewById(R.id.text_buscarTodos)
        buscarTodosViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        map = rootView.findViewById(R.id.map)
        val ruEditText: EditText = rootView.findViewById(R.id.editTextText2)
        val ubicarTodosButton: Button = rootView.findViewById(R.id.button)


        // Configuración inicial del mapa
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(requireContext()))
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        map.isHorizontalMapRepetitionEnabled = false
        map.isVerticalMapRepetitionEnabled = false

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        map.overlays.add(myLocationOverlay)

        // Lógica para el botón "Ubicar a todos"
        ubicarTodosButton.setOnClickListener {
            val ru = ruEditText.text.toString().trim()
            if (ru.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, ingrese un Registro Universitario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            buscarTodasLasUbicaciones(ru)
        }

        return rootView
    }

    private fun buscarTodasLasUbicaciones(ru: String) {
        lifecycleScope.launch {
            try {
                // 1. Limpiar los marcadores anteriores del mapa y de la lista
                clientMarkers.forEach { map.overlays.remove(it) }
                clientMarkers.clear()

                val response = service.getUbicacionTodos(ru)
                if (response.isSuccessful) {
                    val listaUbicaciones = response.body()?.data
                    if (!listaUbicaciones.isNullOrEmpty()) {
                        // 2. Iterar sobre la lista de ubicaciones y crear un marcador para cada una
                        listaUbicaciones.forEach { personaUbicacion ->
                            agregarMarcadorDeCliente(personaUbicacion)
                        }

                        // 3. Centrar el mapa en el primer cliente de la lista como referencia
                        val primerCliente = listaUbicaciones.first()
                        val initialPoint = GeoPoint(primerCliente.latitude, primerCliente.longitude)
                        map.controller.animateTo(initialPoint)
                        map.controller.setZoom(12.0) // Un zoom más alejado para ver varios puntos
                        map.invalidate() // Refrescar el mapa para mostrar todos los marcadores

                        Toast.makeText(requireContext(), "Se encontraron ${listaUbicaciones.size} clientes.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "No se encontraron clientes para el RU proporcionado.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta del servidor: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun agregarMarcadorDeCliente(persona: PersonaUbicacion) {
        val locationPoint = GeoPoint(persona.latitude, persona.longitude)
        val personMarker = Marker(map)
        personMarker.position = locationPoint
        personMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        personMarker.title = "Cod: ${persona.codcli}"
        personMarker.snippet = persona.cliente

        val personIcon = ContextCompat.getDrawable(requireContext(), R.drawable.outline_accessibility_new_24)
        personMarker.icon = personIcon

        map.overlays.add(personMarker)
        clientMarkers.add(personMarker) // Guardar referencia para poder limpiarlo después
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