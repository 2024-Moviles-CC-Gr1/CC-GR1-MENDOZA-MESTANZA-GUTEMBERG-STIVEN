package com.example.recyclerview

import android.content.Intent
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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Llamando métodos para cargar los RecyclerViews y el campo de búsqueda
        initializeAlbumsRV()
        initializePopularAlbumsRV()
        initializeTrendingAlbumsRV()
        initializeSearchView()
    }

    // Método para inicializar el campo de búsqueda
    private fun initializeSearchView() {
        val searchEdt = findViewById<EditText>(R.id.idEdtSearch)
        searchEdt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Llamando al método para buscar canciones
                searchTracks(searchEdt.text.toString())
                true
            } else {
                false
            }
        }
    }

    // Método para abrir la actividad de búsqueda de canciones
    private fun searchTracks(searchQuery: String) {
        val i = Intent(this@MainActivity, SearchActivity::class.java)
        i.putExtra("searchQuery", searchQuery)
        startActivity(i)
    }

    // Método para obtener el token
    private fun getToken(): String {
        val sh = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        return sh.getString("token", "Not Found") ?: "Not Found"
    }

    override fun onStart() {
        super.onStart()
        // Llamando al método para generar el token
        generateToken()
    }

    private fun generateToken() {
        val url = "https://accounts.spotify.com/api/token?grant_type=client_credentials"
        val queue = Volley.newRequestQueue(this@MainActivity)
        val request = object : StringRequest(Method.POST, url, Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)
                val tk = jsonObject.getString("access_token")
                val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
                val myEdit = sharedPreferences.edit()
                myEdit.putString("token", "Bearer $tk")
                myEdit.apply()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(this@MainActivity, "Fail to get response = $error", Toast.LENGTH_SHORT).show()
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Basic MGQ5MDE4MmYwYjUzNDA4MDhmMTM3MTYwMTBiNDc4YWM6NzgzMWVkNDQ3OTcwNDMxMzlkOTA3MDFmZDhlN2M2YzA="
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }
        }
        queue.add(request)
    }

    private fun initializeAlbumsRV() {
        val albumsRV = findViewById<RecyclerView>(R.id.idRVAlbums)
        val albumRVModalArrayList = ArrayList<AlbumRVModal>()
        val albumRVAdapter = AlbumRVAdapter(albumRVModalArrayList, this)
        albumsRV.adapter = albumRVAdapter
        val url = "https://api.spotify.com/v1/albums?ids=2oZSF17FtHQ9sYBscQXoBe%2C0z7bJ6UpjUw8U4TATtc5Ku%2C36UJ90D0e295TvlU109Xvy%2C3uuu6u13U0KeVQsZ3CZKK4%2C45ZIondgVoMB84MQQaUo9T%2C15CyNDuGY5fsG0Hn9rjnpG%2C1HeX4SmCFW4EPHQDvHgrVS%2C6mCDTT1XGTf48p6FkK9qFL"
        val queue = Volley.newRequestQueue(this@MainActivity)
        val albumObjReq = object : JsonObjectRequest(Method.GET, url, null, Response.Listener { response ->
            try {
                val albumArray = response.getJSONArray("albums")
                for (i in 0 until albumArray.length()) {
                    val albumObj = albumArray.getJSONObject(i)
                    val album_type = albumObj.getString("album_type")
                    val artistName = albumObj.getJSONArray("artists").getJSONObject(0).getString("name")
                    val external_ids = albumObj.getJSONObject("external_ids").getString("upc")
                    val external_urls = albumObj.getJSONObject("external_urls").getString("spotify")
                    val href = albumObj.getString("href")
                    val id = albumObj.getString("id")
                    val imgUrl = albumObj.getJSONArray("images").getJSONObject(1).getString("url")
                    val label = albumObj.getString("label")
                    val name = albumObj.getString("name")
                    val popularity = albumObj.getInt("popularity")
                    val release_date = albumObj.getString("release_date")
                    val total_tracks = albumObj.getInt("total_tracks")
                    val type = albumObj.getString("type")
                    albumRVModalArrayList.add(AlbumRVModal(album_type, artistName, external_ids, external_urls, href, id, imgUrl, label, name, popularity, release_date, total_tracks, type))
                }
                albumRVAdapter.notifyDataSetChanged()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(this@MainActivity, "Fail to get data : $error", Toast.LENGTH_SHORT).show()
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = getToken()
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        queue.add(albumObjReq)
    }

    private fun initializePopularAlbumsRV() {
        val albumsRV = findViewById<RecyclerView>(R.id.idRVPopularAlbums)
        val albumRVModalArrayList = ArrayList<AlbumRVModal>()
        val albumRVAdapter = AlbumRVAdapter(albumRVModalArrayList, this)
        albumsRV.adapter = albumRVAdapter
        val url = "https://api.spotify.com/v1/albums?ids=0sjyZypccO1vyihqaAkdt3%2C17vZRWjKOX7TmMktjQL2Qx%2C7lF34sP6HtRAL7VEMvYHff%2C2zXKlf81VmDHIMtQe3oD0r%2C7Gws1vUsWltRs58x8QuYVQ%2C7uftfPn8f7lwtRLUrEVRYM%2C7kSY0fqrPep5vcwOb1juye"
        val queue = Volley.newRequestQueue(this@MainActivity)
        val albumObjReq = object : JsonObjectRequest(Method.GET, url, null, Response.Listener { response ->
            try {
                val albumArray = response.getJSONArray("albums")
                for (i in 0 until albumArray.length()) {
                    val albumObj = albumArray.getJSONObject(i)
                    val album_type = albumObj.getString("album_type")
                    val artistName = albumObj.getJSONArray("artists").getJSONObject(0).getString("name")
                    val external_ids = albumObj.getJSONObject("external_ids").getString("upc")
                    val external_urls = albumObj.getJSONObject("external_urls").getString("spotify")
                    val href = albumObj.getString("href")
                    val id = albumObj.getString("id")
                    val imgUrl = albumObj.getJSONArray("images").getJSONObject(1).getString("url")
                    val label = albumObj.getString("label")
                    val name = albumObj.getString("name")
                    val popularity = albumObj.getInt("popularity")
                    val release_date = albumObj.getString("release_date")
                    val total_tracks = albumObj.getInt("total_tracks")
                    val type = albumObj.getString("type")
                    albumRVModalArrayList.add(AlbumRVModal(album_type, artistName, external_ids, external_urls, href, id, imgUrl, label, name, popularity, release_date, total_tracks, type))
                }
                albumRVAdapter.notifyDataSetChanged()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(this@MainActivity, "Fail to get data : $error", Toast.LENGTH_SHORT).show()
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = getToken()
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        queue.add(albumObjReq)
    }

    private fun initializeTrendingAlbumsRV() {
        val albumsRV = findViewById<RecyclerView>(R.id.idRVTrendingAlbums)
        val albumRVModalArrayList = ArrayList<AlbumRVModal>()
        val albumRVAdapter = AlbumRVAdapter(albumRVModalArrayList, this)
        albumsRV.adapter = albumRVAdapter
        val url = "https://api.spotify.com/v1/albums?ids=0jBNeXfkM5cE3wGxUCZx39%2C6MWtB6iiXyIwun0YzU6DFP%2C5aDEezKnOqyQo0qvTFhpkM%2C50U8k4Hqy3SJmlD98KpklO%2C14kFlB0hThVhygk88n0tuu%2C2yFBOQO1rmH4lVNSFkHx4Y%2C0rbD0sYwWUI2ICZSzM8j56"
        val queue = Volley.newRequestQueue(this@MainActivity)
        val albumObjReq = object : JsonObjectRequest(Method.GET, url, null, Response.Listener { response ->
            try {
                val albumArray = response.getJSONArray("albums")
                for (i in 0 until albumArray.length()) {
                    val albumObj = albumArray.getJSONObject(i)
                    val album_type = albumObj.getString("album_type")
                    val artistName = albumObj.getJSONArray("artists").getJSONObject(0).getString("name")
                    val external_ids = albumObj.getJSONObject("external_ids").getString("upc")
                    val external_urls = albumObj.getJSONObject("external_urls").getString("spotify")
                    val href = albumObj.getString("href")
                    val id = albumObj.getString("id")
                    val imgUrl = albumObj.getJSONArray("images").getJSONObject(1).getString("url")
                    val label = albumObj.getString("label")
                    val name = albumObj.getString("name")
                    val popularity = albumObj.getInt("popularity")
                    val release_date = albumObj.getString("release_date")
                    val total_tracks = albumObj.getInt("total_tracks")
                    val type = albumObj.getString("type")
                    albumRVModalArrayList.add(AlbumRVModal(album_type, artistName, external_ids, external_urls, href, id, imgUrl, label, name, popularity, release_date, total_tracks, type))
                }
                albumRVAdapter.notifyDataSetChanged()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            Toast.makeText(this@MainActivity, "Fail to get data : $error", Toast.LENGTH_SHORT).show()
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = getToken()
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        queue.add(albumObjReq)
    }
}