package com.example.recyclerview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrackRVAdapter(
    private val trackRVModals: ArrayList<TrackRVModal>,
    private val context: Context
) : RecyclerView.Adapter<TrackRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflar el diseño en la vista.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Asignar datos a las vistas de texto.
        val trackRVModal = trackRVModals[position]
        holder.trackNameTV.text = trackRVModal.trackName
        holder.trackArtistTV.text = trackRVModal.trackArtist
        // Añadir listener de clics al item del track.
        holder.itemView.setOnClickListener {
            val trackUrl = "https://open.spotify.com/track/" + trackRVModal.id
            val uri = Uri.parse(trackUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return trackRVModals.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Crear e inicializar las variables para las vistas de texto.
        val trackNameTV: TextView = itemView.findViewById(R.id.idTVTrackName)
        val trackArtistTV: TextView = itemView.findViewById(R.id.idTVTrackArtist)
    }
}