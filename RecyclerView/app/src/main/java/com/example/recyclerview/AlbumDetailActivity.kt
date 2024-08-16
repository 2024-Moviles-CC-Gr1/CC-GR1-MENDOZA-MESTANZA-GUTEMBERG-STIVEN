package com.example.recyclerview

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class AlbumDetailActivity : AppCompatActivity() {

    // Variables
    private var albumID: String = ""
    private var albumImgUrl: String? = null
    private var albumName: String? = null
    private var artist: String? = null
    private var albumUrl: String? = null

    private lateinit var albumNameTV: TextView
    private lateinit var artistTV: TextView
    private lateinit var albumIV: ImageView
    private lateinit var playFAB: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar variables
        setContentView(R.layout.activity_album_detail)
        albumID = intent.getStringExtra("id") ?: ""
        albumIV = findViewById(R.id.idIVAlbum)
        albumImgUrl = intent.getStringExtra("img")
        albumName = intent.getStringExtra("name")
        artist = intent.getStringExtra("artist")
        albumUrl = intent.getStringExtra("albumUrl")
        Log.e("TAG", "album id is : $albumID")
        albumNameTV = findViewById(R.id.idTVAlbumName)
        playFAB = findViewById(R.id.idFABPlay)
        artistTV = findViewById(R.id.idTVArtistName)

        // Asignar datos
        albumNameTV.text = albumName
        artistTV.text = artist

        // Listener para FloatingActionButton
        playFAB.setOnClickListener {
            val uri = Uri.parse(albumUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // Cargar imagen
        Picasso.get().load(albumImgUrl).into(albumIV)

        // Obtener las pistas del álbum
        getAlbumTracks(albumID)
    }

    // Método para obtener el token de acceso
    private fun getToken(): String {
        val sh: SharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        return sh.getString("token", "Not Found") ?: "Not Found"
    }

    private fun getAlbumTracks(albumID: String) {
        val url = "https://api.spotify.com/v1/albums/$albumID/tracks"
        val trackRVModals = ArrayList<TrackRVModal>()
        val trackRVAdapter = TrackRVAdapter(trackRVModals, this)
        val trackRV: RecyclerView = findViewById(R.id.idRVTracks)
        trackRV.adapter = trackRVAdapter
        val queue: RequestQueue = Volley.newRequestQueue(this)

        // Crear solicitud JSON
        val trackObj = object : JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                try {
                    val itemsArray: JSONArray = response.getJSONArray("items")
                    for (i in 0 until itemsArray.length()) {
                        val itemObj: JSONObject = itemsArray.getJSONObject(i)
                        val trackName = itemObj.getString("name")
                        val id = itemObj.getString("id")
                        val trackArtist = itemObj.getJSONArray("artists")
                            .getJSONObject(0).getString("name")

                        // Agregar datos a la lista
                        trackRVModals.add(TrackRVModal(trackName, trackArtist, id))
                    }
                    trackRVAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Fail to get Tracks: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = getToken()
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        // Agregar solicitud a la cola
        queue.add(trackObj)
    }
}