fun main() {
    val dataManager = DataManager()
    while (true) {
        println("Choose an option: [1] Add Recipe, [2] Delete Recipe, [3] Update Recipe, [4] List Recipes, [5] Exit")
        when (readLine()!!) {
            "1" -> {
                println("Enter recipe name:")
                val name = readLine()!!
                println("Is the recipe public? (true/false):")
                val isPublic = readLine()!!.toBoolean()
                println("Enter the rating of the recipe:")
                val rating = readLine()!!.toDouble()
                val creationDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
                val ingredients = mutableListOf<Ingredient>()
                val recipe = Recipe(name, ingredients.size, isPublic, creationDate, rating, ingredients)
                dataManager.addRecipe(recipe)
            }
            "2" -> {
                println("Enter the name of the recipe to delete:")
                val name = readLine()!!
                dataManager.deleteRecipe(name)
            }
            "3" -> {
                println("Enter the name of the recipe to update:")
                val oldName = readLine()!!
                println("Enter new name for the recipe:")
                val newName = readLine()!!
                println("Is the recipe public? (true/false):")
                val isPublic = readLine()!!.toBoolean()
                println("Enter the new rating for the recipe:")
                val rating = readLine()!!.toDouble()
                val creationDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
                val ingredients = mutableListOf<Ingredient>() // Ideally, add a mechanism to edit ingredients
                val updatedRecipe = Recipe(newName, ingredients.size, isPublic, creationDate, rating, ingredients)
                dataManager.updateRecipe(oldName, updatedRecipe)
            }
            "4" -> {
                val recipes = dataManager.readRecipes()
                if (recipes.isEmpty()) {
                    println("No recipes available.")
                } else {
                    recipes.forEach { println(it) }
                }
            }
            "5" -> break
        }
    }
}
