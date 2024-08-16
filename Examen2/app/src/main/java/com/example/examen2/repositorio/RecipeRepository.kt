package com.example.examen2.repositorio


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.examen2.models.Ingredient
import com.example.examen2.models.Recipe


class RecipeRepository(
    contexto: Context?,
) : SQLiteOpenHelper(
    contexto,
    "recipe_ingredient_db",
    null,
    2
) {
    override fun onCreate(db: SQLiteDatabase?) {
        // Crear las tablas con las nuevas columnas
        val createRecipeTable = """
            CREATE TABLE RECIPE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(50),
                ingredientCount INTEGER,
                isPublic INTEGER,
                creationDate TEXT,
                rating REAL
            )
        """.trimIndent()

        val createIngredientTable = """
            CREATE TABLE INGREDIENT (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(50),
                amount REAL,
                unit VARCHAR(20),
                essential INTEGER,
                cost REAL,
                recipe_id INTEGER,
                latitud REAL,
                longitud REAL,
                FOREIGN KEY (recipe_id) REFERENCES RECIPE(id)
            )
        """.trimIndent()

        db?.execSQL(createRecipeTable)
        db?.execSQL(createIngredientTable)

        val insertInitialRecipes = """
            INSERT INTO RECIPE (name, ingredientCount, isPublic, creationDate, rating)
            VALUES ('Recipe1', 3, 1, '2024-01-01', 4.5),
                   ('Recipe2', 5, 0, '2024-02-01', 3.8);
        """.trimIndent()
        db?.execSQL(insertInitialRecipes)

        val insertInitialIngredients = """
            INSERT INTO INGREDIENT (name, amount, unit, essential, cost, recipe_id, latitud, longitud)
            VALUES ('Sugar', 1.0, 'kg', 1, 1.5, 1, -0.2072588101453826, -78.50641206834409),
                    ('Salt', 0.5, 'kg', 1, 0.8, 2, -0.20725878729057573, -78.49984595809967);
""".trimIndent()
        db?.execSQL(insertInitialIngredients)
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion <= 2) {
            db?.execSQL("DROP TABLE IF EXISTS INGREDIENT")
            db?.execSQL("DROP TABLE IF EXISTS RECIPE")
            onCreate(db)
        }
    }

    fun crearRecipe(newRecipe: Recipe): Boolean {
        val basedatosEscritura = writableDatabase
        val valoresAGuardar = ContentValues()

        valoresAGuardar.put("name", newRecipe.name)
        valoresAGuardar.put("ingredientCount", newRecipe.ingredientCount)
        valoresAGuardar.put("isPublic", if (newRecipe.isPublic) 1 else 0)
        valoresAGuardar.put("creationDate", newRecipe.creationDate)
        valoresAGuardar.put("rating", newRecipe.rating)

        val resultadoGuardar = basedatosEscritura.insert(
            "RECIPE", //nombre de la tabla
            null,
            valoresAGuardar //valores
        )

        basedatosEscritura.close()
        return resultadoGuardar != -1L
    }

    fun obtenerRecipes(): ArrayList<Recipe> {
        val baseDatosLectura = readableDatabase

        val scriptConsultaLectura = """
            SELECT * FROM RECIPE
        """.trimIndent()

        val resultadoConsulta = baseDatosLectura.rawQuery(scriptConsultaLectura, null)

        val recipes = arrayListOf<Recipe>()

        if (resultadoConsulta != null && resultadoConsulta.moveToFirst()) {
            do {
                val id = resultadoConsulta.getInt(0)
                val name = resultadoConsulta.getString(1)
                val ingredientCount = resultadoConsulta.getInt(2)
                val isPublic = resultadoConsulta.getInt(3) == 1
                val creationDate = resultadoConsulta.getString(4)
                val rating = resultadoConsulta.getDouble(5)

                val recipeEncontrado = Recipe(
                    id = id,
                    name = name,
                    ingredientCount = ingredientCount,
                    isPublic = isPublic,
                    creationDate = creationDate,
                    rating = rating
                )
                recipes.add(recipeEncontrado)
            } while (resultadoConsulta.moveToNext())
        }

        resultadoConsulta?.close()
        baseDatosLectura.close()
        return recipes
    }

    fun consultarRecipePorId(id: Int): Recipe {
        val baseDatosLectura = readableDatabase

        val scriptConsultaLectura = """
            SELECT * FROM RECIPE WHERE ID = ?
        """.trimIndent()

        val parametrosConsultaLectura = arrayOf(id.toString())

        val resultadoConsultaLectura = baseDatosLectura.rawQuery(
            scriptConsultaLectura,
            parametrosConsultaLectura
        )
        Log.i("datoRecibido", "${id}")

        val recipeEncontrado = Recipe(
            id = 0,
            name = "Nombre",
            ingredientCount = 0,
            isPublic = false,
            creationDate = "01/01/2000",
            rating = 0.0
        )

        if (resultadoConsultaLectura.moveToFirst()) {
            recipeEncontrado.id = resultadoConsultaLectura.getInt(0)
            recipeEncontrado.name = resultadoConsultaLectura.getString(1)
            Log.i("encontrado", "${recipeEncontrado.name}")
            recipeEncontrado.ingredientCount = resultadoConsultaLectura.getInt(2)
            recipeEncontrado.isPublic = resultadoConsultaLectura.getInt(3) == 1
            recipeEncontrado.creationDate = resultadoConsultaLectura.getString(4)
            recipeEncontrado.rating = resultadoConsultaLectura.getDouble(5)
        }

        resultadoConsultaLectura.close()
        baseDatosLectura.close()
        return recipeEncontrado
    }

    fun actualizarRecipePorId(datosActualizados: Recipe): Boolean {
        val conexionEscritura = writableDatabase
        val valoresAActualizar = ContentValues()

        valoresAActualizar.put("name", datosActualizados.name)
        valoresAActualizar.put("ingredientCount", datosActualizados.ingredientCount)
        valoresAActualizar.put("isPublic", if (datosActualizados.isPublic) 1 else 0)
        valoresAActualizar.put("creationDate", datosActualizados.creationDate)
        valoresAActualizar.put("rating", datosActualizados.rating)

        val parametrosConsultaActualizar = arrayOf(datosActualizados.id.toString())
        val resultadoActualizcion = conexionEscritura.update(
            "RECIPE", //tabla
            valoresAActualizar,
            "id = ?",
            parametrosConsultaActualizar
        )

        conexionEscritura.close()
        return resultadoActualizcion != -1
    }

    fun eliminarRecipePorId(id: Int): Boolean {
        val conexionEscritura = writableDatabase

        val parametrosConsultaDelete = arrayOf(id.toString())

        val resultadoEliminacion = conexionEscritura.delete(
            "RECIPE", //tabla
            "id = ?",
            parametrosConsultaDelete
        )

        conexionEscritura.close()
        return resultadoEliminacion != -1
    }
}



class IngredientRepository(
    contexto: Context?
) : SQLiteOpenHelper(
    contexto,
    "recipe_ingredient_db",
    null,
    2 // Incrementa la versión aquí para reflejar los cambios en el esquema
) {
    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun crearIngredient(newIngredient: Ingredient): Boolean {
        val basedatosEscritura = writableDatabase
        val valoresAGuardar = ContentValues()

        valoresAGuardar.put("name", newIngredient.name)
        valoresAGuardar.put("amount", newIngredient.amount)
        valoresAGuardar.put("unit", newIngredient.unit)
        valoresAGuardar.put("essential", if (newIngredient.essential) 1 else 0)
        valoresAGuardar.put("cost", newIngredient.cost)
        valoresAGuardar.put("recipe_id", newIngredient.recipe_id)
        valoresAGuardar.put("latitud", newIngredient.latitud)
        valoresAGuardar.put("longitud", newIngredient.longitud)

        val resultadoGuardar = basedatosEscritura.insert(
            "INGREDIENT",
            null,
            valoresAGuardar
        )

        basedatosEscritura.close()
        return resultadoGuardar != -1L
    }

    fun obtenerIngredients(): ArrayList<Ingredient> {
        val baseDatosLectura = readableDatabase

        val scriptConsultaLectura = """
            SELECT * FROM INGREDIENT
        """.trimIndent()

        val resultadoConsulta = baseDatosLectura.rawQuery(scriptConsultaLectura, null)

        val ingredients = arrayListOf<Ingredient>()

        if (resultadoConsulta != null && resultadoConsulta.moveToFirst()) {
            do {
                val id = resultadoConsulta.getInt(0)
                val name = resultadoConsulta.getString(1)
                val amount = resultadoConsulta.getDouble(2)
                val unit = resultadoConsulta.getString(3)
                val essential = resultadoConsulta.getInt(4) == 1
                val cost = resultadoConsulta.getDouble(5)
                val recipe_id = resultadoConsulta.getInt(6)
                val latitud = resultadoConsulta.getDouble(7)
                val longitud = resultadoConsulta.getDouble(8)

                val ingredientEncontrado = Ingredient(
                    id = id,
                    name = name,
                    amount = amount,
                    unit = unit,
                    essential = essential,
                    cost = cost,
                    recipe_id = recipe_id,
                    latitud = latitud,
                    longitud = longitud
                )
                ingredients.add(ingredientEncontrado)
            } while (resultadoConsulta.moveToNext())
        }

        resultadoConsulta?.close()
        baseDatosLectura.close()
        return ingredients
    }

    fun obtenerIngredientsPorRecipeId(recipeId: Int): ArrayList<Ingredient> {
        val baseDatosLectura = readableDatabase

        val scriptConsultaLectura = """
            SELECT * FROM INGREDIENT
            WHERE recipe_id = ?
        """.trimIndent()

        val resultadoConsulta = baseDatosLectura.rawQuery(scriptConsultaLectura, arrayOf(recipeId.toString()))

        val ingredients = arrayListOf<Ingredient>()

        if (resultadoConsulta != null && resultadoConsulta.moveToFirst()) {
            do {
                val id = resultadoConsulta.getInt(0)
                val name = resultadoConsulta.getString(1)
                val amount = resultadoConsulta.getDouble(2)
                val unit = resultadoConsulta.getString(3)
                val essential = resultadoConsulta.getInt(4) == 1
                val cost = resultadoConsulta.getDouble(5)
                val recipe_id = resultadoConsulta.getInt(6)
                val latitud = resultadoConsulta.getDouble(7)
                val longitud = resultadoConsulta.getDouble(8)

                val ingredientEncontrado = Ingredient(
                    id = id,
                    name = name,
                    amount = amount,
                    unit = unit,
                    essential = essential,
                    cost = cost,
                    recipe_id = recipe_id,
                    latitud = latitud,
                    longitud = longitud
                )
                ingredients.add(ingredientEncontrado)
            } while (resultadoConsulta.moveToNext())
        }

        resultadoConsulta?.close()
        baseDatosLectura.close()
        return ingredients
    }

    fun consultarIngredientPorId(id: Int): Ingredient {
        val baseDatosLectura = readableDatabase

        val scriptConsultaLectura = """
            SELECT * FROM INGREDIENT WHERE id = ?
        """.trimIndent()

        val parametrosConsultaLectura = arrayOf(id.toString())

        val resultadoConsultaLectura = baseDatosLectura.rawQuery(
            scriptConsultaLectura,
            parametrosConsultaLectura
        )

        val ingredientEncontrado = Ingredient(
            id = 0,
            name = "Nombre",
            amount = 0.0,
            unit = "Unit",
            essential = false,
            cost = 0.0,
            recipe_id = 0,
            latitud = 0.0,
            longitud = 0.0
        )

        if (resultadoConsultaLectura.moveToFirst()) {
            ingredientEncontrado.id = resultadoConsultaLectura.getInt(0)
            ingredientEncontrado.name = resultadoConsultaLectura.getString(1)
            ingredientEncontrado.amount = resultadoConsultaLectura.getDouble(2)
            ingredientEncontrado.unit = resultadoConsultaLectura.getString(3)
            ingredientEncontrado.essential = resultadoConsultaLectura.getInt(4) == 1
            ingredientEncontrado.cost = resultadoConsultaLectura.getDouble(5)
            ingredientEncontrado.recipe_id = resultadoConsultaLectura.getInt(6)
            ingredientEncontrado.latitud = resultadoConsultaLectura.getDouble(7)
            ingredientEncontrado.longitud = resultadoConsultaLectura.getDouble(8)
        }

        resultadoConsultaLectura.close()
        baseDatosLectura.close()
        return ingredientEncontrado
    }

    fun actualizarIngredientPorId(datosActualizados: Ingredient): Boolean {
        val conexionEscritura = writableDatabase
        val valoresAActualizar = ContentValues()

        valoresAActualizar.put("name", datosActualizados.name)
        valoresAActualizar.put("amount", datosActualizados.amount)
        valoresAActualizar.put("unit", datosActualizados.unit)
        valoresAActualizar.put("essential", if (datosActualizados.essential) 1 else 0)
        valoresAActualizar.put("cost", datosActualizados.cost)
        valoresAActualizar.put("recipe_id", datosActualizados.recipe_id)
        valoresAActualizar.put("latitud", datosActualizados.latitud)
        valoresAActualizar.put("longitud", datosActualizados.longitud)

        val parametrosConsultaActualizar = arrayOf(datosActualizados.id.toString())
        val resultadoActualizcion = conexionEscritura.update(
            "INGREDIENT", // tabla
            valoresAActualizar,
            "id = ?",
            parametrosConsultaActualizar
        )

        conexionEscritura.close()
        return resultadoActualizcion != -1
    }

    fun eliminarIngredientPorId(id: Int): Boolean {
        val conexionEscritura = writableDatabase

        val parametrosConsultaDelete = arrayOf(id.toString())

        val resultadoEliminacion = conexionEscritura.delete(
            "INGREDIENT", // tabla
            "id = ?",
            parametrosConsultaDelete
        )

        conexionEscritura.close()
        return resultadoEliminacion != -1
    }
}

