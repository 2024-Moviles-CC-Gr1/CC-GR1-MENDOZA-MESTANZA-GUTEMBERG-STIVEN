package com.example.examen2


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.example.examen2.BDD
import com.example.examen2.models.Ingredient
import com.example.examen2.repositorio.IngredientRepository
import com.example.examen2.repositorio.RecipeRepository
import com.google.android.material.snackbar.Snackbar

class BIngre : AppCompatActivity() {

    private lateinit var listViewU: ListView
    var datoRec = 0 // dato que sale del anterior activity



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_b_ingre)

        BDD.CompbddAplicacion = RecipeRepository(this)
        BDD.CompbddAplicacionB = IngredientRepository(this)
        datoRec = intent.getIntExtra("REC_ID",1)

        BDD.CompbddAplicacionB!!.consultarIngredientPorId(datoRec)
        listViewU = findViewById(R.id.lv_ingre)

    }


    override fun onStart() {
        super.onStart()
        Log.i("ciclo-vida", "onStart")



        val ingredients = BDD.CompbddAplicacionB!!.obtenerIngredients()

        val botonTo = findViewById<Button>(R.id.buttonToRec)

        val rec = BDD.CompbddAplicacion!!.consultarRecipePorId(datoRec)

        botonTo.text = rec.name


        actualizarListaIngredientes(datoRec)

        configurarBotonAgregarIngredient()


        botonTo.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun actualizarListaIngredientes(recipeId: Int) {
        val ingredientes = BDD.CompbddAplicacionB!!.obtenerIngredientsPorRecipeId(recipeId)

        val ingredientesString = ingredientes.map { ingredient ->
            "Nombre: ${ingredient.name}, Cantidad: ${ingredient.amount} ${ingredient.unit}, Esencial: ${if (ingredient.essential) "Sí" else "No"}, Costo: ${ingredient.cost}"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ingredientesString)
        listViewU.adapter = adapter
        adapter.notifyDataSetChanged()
        registerForContextMenu(listViewU)
    }


    var posicionItemSeleccionado = 0
    var idItemSeleccionado = 0
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        val item = menu?.findItem(R.id.menu_ver)
        if (item != null) {
            item.title = "Ver en el mapa"
        }

        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        Log.d("IDen", "${info}")
        val posicion = info.position

        val ingred = BDD.CompbddAplicacionB!!.obtenerIngredients()

        idItemSeleccionado = ingred.getOrNull(posicion)?.id ?: 0


    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_editar -> {


                val ingredientSeleccionado = BDD.CompbddAplicacionB!!.consultarIngredientPorId(idItemSeleccionado)

                // Inflar el layout del formulario de edición
                val formularioEdicion = layoutInflater.inflate(R.layout.formulario_ingre, null)

                // Obtener referencias a las vistas del formulario
                val editTextName: EditText = formularioEdicion.findViewById(R.id.editTextNombreIngrediente)
                val editTextAmount: EditText = formularioEdicion.findViewById(R.id.editTextCantidad)
                val editTextUnit: EditText = formularioEdicion.findViewById(R.id.editTextUnidad)
                val checkBoxEssential: CheckBox = formularioEdicion.findViewById(R.id.checkBoxEsencial)
                val editTextCost: EditText = formularioEdicion.findViewById(R.id.editTextCosto)
                val editTextLatitud: EditText = formularioEdicion.findViewById(R.id.editTextLatitud)
                val editTextLongitud: EditText = formularioEdicion.findViewById(R.id.editTextLongitud)

                val buttonGuardar: Button = formularioEdicion.findViewById(R.id.buttonGuardarIngrediente)
                buttonGuardar.visibility = View.GONE

                // Prellenar el formulario con los datos del Ingredient
                editTextName.setText(ingredientSeleccionado.name)
                editTextAmount.setText(ingredientSeleccionado.amount.toString())
                editTextUnit.setText(ingredientSeleccionado.unit)
                checkBoxEssential.isChecked = ingredientSeleccionado.essential
                editTextCost.setText(ingredientSeleccionado.cost.toString())
                editTextLatitud.setText(ingredientSeleccionado.latitud.toString())
                editTextLongitud.setText(ingredientSeleccionado.longitud.toString())

                // Crear el diálogo de edición
                val dialog = AlertDialog.Builder(this)
                    .setView(formularioEdicion)
                    .setPositiveButton("Guardar") { _, _ ->
                        // Obtener los datos actualizados del formulario
                        val nuevoNombre = editTextName.text.toString()
                        val nuevoAmount = editTextAmount.text.toString().toDoubleOrNull() ?: 0.0
                        val nuevoUnit = editTextUnit.text.toString()
                        val nuevoEssential = checkBoxEssential.isChecked
                        val nuevoCost = editTextCost.text.toString().toDoubleOrNull() ?: 0.0
                        val nuevaLatitud = editTextLatitud.text.toString().toDoubleOrNull() ?: 0.0
                        val nuevaLongitud = editTextLongitud.text.toString().toDoubleOrNull() ?: 0.0

                        val ingredientActualizado = Ingredient(
                            id = ingredientSeleccionado.id,
                            name = nuevoNombre,
                            amount = nuevoAmount,
                            unit = nuevoUnit,
                            essential = nuevoEssential,
                            cost = nuevoCost,
                            recipe_id = ingredientSeleccionado.recipe_id,
                            latitud = nuevaLatitud,
                            longitud = nuevaLongitud
                        )

                        BDD.CompbddAplicacionB!!.actualizarIngredientPorId(ingredientActualizado)

                        actualizarListaIngredientes(datoRec)
                    }
                    .setNegativeButton("Cancelar", null)
                    .create()
                dialog.show()


                return true
            }
            R.id.menu_eliminar ->{

                val exito = BDD.CompbddAplicacionB?.eliminarIngredientPorId(idItemSeleccionado)
                if (exito == true) {
                    Toast.makeText(this, "Receta eliminada con éxito", Toast.LENGTH_SHORT).show()
                    // Actualiza la interfaz para reflejar la eliminación
                    actualizarListaIngredientes(datoRec)
                } else {
                    Toast.makeText(this, "Error al eliminar la receta", Toast.LENGTH_SHORT).show()
                }





                return true
            }

            R.id.menu_ver -> {

                if(idItemSeleccionado == 0){
                    Log.d("Cero", "${0}")
                }else{
                    val intent = Intent(this, GGoogleMaps2::class.java)

                    intent.putExtra("INGR_ID", idItemSeleccionado)
                    Log.i("enviadoINGR", idItemSeleccionado.toString())

                    startActivity(intent)
                }


                return true
            }

            else -> super.onContextItemSelected(item)
        }
    }




    private fun configurarBotonAgregarIngredient() {
        val botonAgregarIngredient = findViewById<Button>(R.id.btn_anadir_ingre)

        botonAgregarIngredient.setOnClickListener {
            // Inflar el nuevo layout (formulario_ingredient.xml)
            val formularioIngredient = layoutInflater.inflate(R.layout.formulario_ingre, null)

            // Obtener referencias a las vistas en el nuevo layout
            val editTextNombre: EditText = formularioIngredient.findViewById(R.id.editTextNombreIngrediente)
            val editTextCantidad: EditText = formularioIngredient.findViewById(R.id.editTextCantidad)
            val editTextUnidad: EditText = formularioIngredient.findViewById(R.id.editTextUnidad)
            val checkBoxEsencial: CheckBox = formularioIngredient.findViewById(R.id.checkBoxEsencial)
            val editTextCosto: EditText = formularioIngredient.findViewById(R.id.editTextCosto)
            val editTextLatitud: EditText = formularioIngredient.findViewById(R.id.editTextLatitud)
            val editTextLongitud: EditText = formularioIngredient.findViewById(R.id.editTextLongitud)
            val buttonGuardar: Button = formularioIngredient.findViewById(R.id.buttonGuardarIngrediente)
            buttonGuardar.visibility = View.GONE

            val dialog = AlertDialog.Builder(this)
                .setView(formularioIngredient)
                .setPositiveButton("Guardar") { _, _ ->
                    val nombre = editTextNombre.text.toString()
                    val cantidad = editTextCantidad.text.toString().toDoubleOrNull() ?: 0.0
                    val unidad = editTextUnidad.text.toString()
                    val esencial = checkBoxEsencial.isChecked
                    val costo = editTextCosto.text.toString().toDoubleOrNull() ?: 0.0
                    val latitud = editTextLatitud.text.toString().toDoubleOrNull() ?: 0.0
                    val longitud = editTextLongitud.text.toString().toDoubleOrNull() ?: 0.0

                    // Obtener el ID de la receta actual (este ID debería ser proporcionado desde otra parte del código)
                    val idReceta = datoRec // Asegúrate de implementar este método

                    val nuevoIngredient = Ingredient(
                        id = 0,
                        name = nombre,
                        amount = cantidad,
                        unit = unidad,
                        essential = esencial,
                        cost = costo,
                        recipe_id = idReceta,
                        latitud = latitud,
                        longitud = longitud
                    )

                    BDD.CompbddAplicacionB!!.crearIngredient(nuevoIngredient)

                    actualizarListaIngredientes(datoRec)
                }
                .setNegativeButton("Cancelar", null)
                .create()
            dialog.show()
        }
    }

}