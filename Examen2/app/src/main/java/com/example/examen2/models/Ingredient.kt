package com.example.examen2.models

data class Ingredient(
    var id: Int = 0,
    var name: String,
    var amount: Double,
    var unit: String,
    var essential: Boolean,
    var cost: Double,
    var recipe_id: Int,
    var latitud: Double,
    var longitud: Double
)