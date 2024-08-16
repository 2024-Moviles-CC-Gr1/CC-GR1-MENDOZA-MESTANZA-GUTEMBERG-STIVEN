package com.example.examen2


import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.examen2.models.Ingredient
import com.example.examen2.repositorio.IngredientRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar

class GGoogleMaps2 : AppCompatActivity() {
    private lateinit var mapa: GoogleMap
    var permisos = false
    var datoIngre = 0
    private lateinit var ingredient: Ingredient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ggoogle_maps2)
        datoIngre = intent.getIntExtra("INGR_ID", 1)
        BDD.CompbddAplicacionB = IngredientRepository(this)

        solicitarPermisos()
        iniciarLogicaMapa()
    }

    fun solicitarPermisos() {
        val contexto = this.applicationContext
        val nombrePermisoFine = android.Manifest.permission.ACCESS_FINE_LOCATION
        val nombrePermisoCoarse = android.Manifest.permission.ACCESS_COARSE_LOCATION
        val permisoFine = ContextCompat.checkSelfPermission(contexto, nombrePermisoFine)
        val permisoCoarse = ContextCompat.checkSelfPermission(contexto, nombrePermisoCoarse)
        val tienePermisos = permisoFine == PackageManager.PERMISSION_GRANTED &&
                permisoCoarse == PackageManager.PERMISSION_GRANTED
        if (tienePermisos) {
            permisos = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(nombrePermisoFine, nombrePermisoCoarse), 1
            )
        }
    }

    fun iniciarLogicaMapa(){
        val fragmentoMapa = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        fragmentoMapa.getMapAsync { googleMap ->
            with(googleMap){
                mapa = googleMap
                establecerConfiguracionMapa()
                mostrarUbicacionIngrediente()
                escucharListeners()
            }
        }
    }


    fun mostrarUbicacionIngrediente() {
        val ingrediente = BDD.CompbddAplicacionB!!.consultarIngredientPorId(datoIngre)

        // Asegúrate de que el ingrediente tenga latitud y longitud válidas
        if (ingrediente.latitud != 0.0 && ingrediente.longitud != 0.0) {
            val ubicacion = LatLng(ingrediente.latitud, ingrediente.longitud)
            val marcador = anadirMarcador(ubicacion, ingrediente.name)
            moverCamaraConZoom(ubicacion, 17f)

            // Configurar InfoWindow
            mapa.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                override fun getInfoWindow(marker: Marker): View? {
                    return null // Usa el layout personalizado en getInfoContents
                }

                override fun getInfoContents(marker: Marker): View {
                    Log.d("GGoogleMaps2", "InfoWindow: getInfoContents called")
                    val infoView = layoutInflater.inflate(R.layout.info_window_layout, null)
                    val nombre = infoView.findViewById<TextView>(R.id.infoNombre)
                    val cantidad = infoView.findViewById<TextView>(R.id.infoCantidad)
                    val unidad = infoView.findViewById<TextView>(R.id.infoUnidad)
                    val costo = infoView.findViewById<TextView>(R.id.infoCosto)

                    nombre.text = ingrediente.name
                    cantidad.text = "Cantidad: ${ingrediente.amount}"
                    unidad.text = "Unidad: ${ingrediente.unit}"
                    costo.text = "Costo: ${ingrediente.cost}"

                    return infoView
                }
            })

            // Mostrar el InfoWindow del marcador
            marcador.showInfoWindow()
        } else {
            mostrarSnackbar("Ubicación del ingrediente no válida")
        }
    }

    fun establecerConfiguracionMapa() {
        val contexto = this.applicationContext
        with(mapa) {
            val nombrePermisoFine = android.Manifest.permission.ACCESS_FINE_LOCATION
            val nombrePermisoCoarse = android.Manifest.permission.ACCESS_COARSE_LOCATION
            val permisoFine = ContextCompat.checkSelfPermission(contexto, nombrePermisoFine)
            val permisoCoarse = ContextCompat.checkSelfPermission(contexto, nombrePermisoCoarse)
            val tienePermisos = permisoFine == PackageManager.PERMISSION_GRANTED &&
                    permisoCoarse == PackageManager.PERMISSION_GRANTED
            if (tienePermisos) {
                mapa.isMyLocationEnabled = true
                uiSettings.isMyLocationButtonEnabled = true
            }
            uiSettings.isZoomControlsEnabled = true
        }
    }

    fun moverCamaraConZoom(latLang: LatLng, zoom: Float = 10f) {
        mapa.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLang, zoom
            )
        )
    }

    fun anadirMarcador(latLang: LatLng, title: String): Marker {
        return mapa.addMarker(
            MarkerOptions().position(latLang)
                .title(title)
        )!!
    }

    fun mostrarSnackbar(texto: String) {
        val snack = Snackbar.make(
            findViewById(R.id.cl_google_maps),
            texto,
            Snackbar.LENGTH_INDEFINITE
        )
        snack.show()
    }

    fun escucharListeners() {
        mapa.setOnMarkerClickListener {
            mostrarSnackbar("setOnMarkerClickListener ${it.tag}")
            return@setOnMarkerClickListener true
        }
        mapa.setOnCameraMoveListener {
            mostrarSnackbar("setOnCameraMoveListener")
        }
        mapa.setOnCameraMoveStartedListener {
            mostrarSnackbar("setOnCameraMoveStartedListener")
        }
        mapa.setOnCameraIdleListener {
            mostrarSnackbar("setOnCameraIdleListener")
        }
    }
}
