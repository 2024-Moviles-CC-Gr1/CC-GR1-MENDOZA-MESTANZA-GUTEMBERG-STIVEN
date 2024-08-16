package com.example.examen2.models

data class Recipe(
    var id: Int = 0,
    var name: String,
    var ingredientCount: Int,
    var isPublic: Boolean,
    var creationDate: String,
    var rating: Double)