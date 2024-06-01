import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class DataManager {
    private val gson = Gson()
    private val filePath = "src/main/resources/recetas.json"

    init {
        val file = File(filePath)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeText("[]")
        }
    }

    fun readRecipes(): MutableList<Recipe> {
        val jsonString = File(filePath).readText()
        val itemType = object : TypeToken<MutableList<Recipe>>() {}.type
        return gson.fromJson(jsonString, itemType) ?: mutableListOf()
    }

    fun addRecipe(recipe: Recipe) {
        val recipes = readRecipes()
        recipes.add(recipe)
        saveRecipes(recipes)
    }

    fun updateRecipe(oldName: String, updatedRecipe: Recipe) {
        val recipes = readRecipes()
        recipes.replaceAll { if (it.name == oldName) updatedRecipe else it }
        saveRecipes(recipes)
    }

    fun deleteRecipe(name: String) {
        val recipes = readRecipes()
        recipes.removeAll { it.name == name }
        saveRecipes(recipes)
    }

    fun saveRecipes(recipes: MutableList<Recipe>) {
        val jsonString = gson.toJson(recipes)
        File(filePath).writeText(jsonString)
    }
}
