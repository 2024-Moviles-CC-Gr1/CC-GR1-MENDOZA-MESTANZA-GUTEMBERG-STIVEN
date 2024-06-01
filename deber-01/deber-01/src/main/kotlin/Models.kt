data class Recipe(
    var name: String,
    var ingredientCount: Int,
    var isPublic: Boolean,
    var creationDate: String,
    var rating: Double,
    var ingredients: MutableList<Ingredient>
)

data class Ingredient(
    var name: String,
    var amount: Double,
    var unit: String,
    var essential: Boolean,
    var cost: Double
)
