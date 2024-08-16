package com.example.examen2

import com.example.examen2.repositorio.IngredientRepository
import com.example.examen2.repositorio.RecipeRepository

class BDD {

    companion object {
        var CompbddAplicacion: RecipeRepository? = null
        var CompbddAplicacionB: IngredientRepository? = null
    }
}