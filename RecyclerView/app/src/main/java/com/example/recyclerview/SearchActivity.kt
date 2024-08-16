package com.example.recyclerview


import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {

    // Variables
    private var searchQuery: String = ""
    private lateinit var searchEdt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Inicializar variables
        searchEdt = findViewById(R.id.idEdtSearch)
        searchQuery = intent.getStringExtra("searchQuery") ?: ""
        searchEdt.setText(searchQuery)

        // Listener para EditText
        searchEdt.setOnEditorActionListener { v: TextView, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getTracks(searchEdt.text.toString())
                true
            } else {
                false
            }
        }

        // Obtener pistas
        getTracks(searchQuery)
    }

    // Método para obtener el token
    private fun getToken(): String {
        val sh: SharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        return sh.getString("token", "Not Found") ?: "Not Found"
    }

    private fun getTracks(searchQuery: String) {
        // Inicializar RecyclerView, lista y adaptador
        val songsRV: RecyclerView = findViewById(R.id.idRVSongs)
        val trackRVModals = ArrayList<TrackRVModal>()
        val trackRVAdapter = TrackRVAdapter(trackRVModals, this)
        songsRV.adapter = trackRVAdapter

        // URL para la solicitud
        val url = "https://api.spotify.com/v1/search?q=$searchQuery&type=track"
        val queue: RequestQueue = Volley.newRequestQueue(this)

        // Solicitud JSON
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                try {
                    val trackObj: JSONObject = response.getJSONObject("tracks")
                    val itemsArray: JSONArray = trackObj.getJSONArray("items")
                    for (i in 0 until itemsArray.length()) {
                        val itemObj: JSONObject = itemsArray.getJSONObject(i)
                        val trackName = itemObj.getString("name")
                        val trackArtist = itemObj.getJSONArray("artists").getJSONObject(0).getString("name")
                        val trackID = itemObj.getString("id")

                        // Agregar datos a la lista
                        trackRVModals.add(TrackRVModal(trackName, trackArtist, trackID))
                    }
                    // Notificar al adaptador
                    trackRVAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Fail to get data: ${error.message}", Toast.LENGTH_SHORT).show()
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
        queue.add(jsonObjectRequest)
    }
}