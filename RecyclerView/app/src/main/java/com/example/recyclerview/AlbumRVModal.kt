package com.example.recyclerview

data class AlbumRVModal(
    val album_type: String,
    val artistName: String,
    val external_ids: String,
    val external_urls: String,
    val href: String,
    val id: String,
    val imageUrl: String,
    val label: String,
    val name: String,
    val popularity: Int,
    val release_date: String,
    val total_tracks: Int,
    val type: String
)