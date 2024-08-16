package com.example.examen2

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
import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.runtime.NoLiveLiterals
import com.example.examen2.models.Recipe
import com.example.examen2.repositorio.RecipeRepository
import com.example.examen2.BIngre

class MainActivity : AppCompatActivity() {


    private lateinit var listViewU: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

        BDD.CompbddAplicacion = RecipeRepository(this)

        listViewU = findViewById(R.id.lv_list_view)

        configurarBotonAgregarReceta()



    }

    override fun onStart() {
        super.onStart()
        Log.i("ciclo-vida", "onStart")

        actualizarListaRecetas()
    }

    private fun actualizarListaRecetas() {
        val recipes = BDD.CompbddAplicacion!!.obtenerRecipes()

        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            recipes.map { it.name }
        )

        listViewU.adapter = adaptador
        adaptador.notifyDataSetChanged()

        registerForContextMenu(listViewU)
    }


    // talves se necesite el saveInstance para que se vea actualizado los datos


    var posicionItemSeleccionado = 0
    var idItemSeleccionado = 0
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        // Obtener el id del ArrayListSeleccionado
        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        Log.d("IDen", "${info}")
        val posicion = info.position
        posicionItemSeleccionado = posicion

        val recetas = BDD.CompbddAplicacion?.obtenerRecipes()
        idItemSeleccionado = recetas?.getOrNull(posicion)?.id ?: 0
    }


    @NoLiveLiterals
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_editar -> {

                val formularioBase = layoutInflater.inflate(R.layout.formulario_base, null)
                setContentView(formularioBase)

                // Obtener referencias a las vistas en el layout de Recipe
                val editTextNombreReceta: EditText = findViewById(R.id.editTextNombreReceta)
                val editTextCantidadIngredientes: EditText = findViewById(R.id.editTextCantidadIngredientes)
                val checkBoxPublica: CheckBox = findViewById(R.id.checkBoxPublica)
                val editTextFechaCreacion: EditText = findViewById(R.id.editTextFechaCreacion)
                val editTextCalificacion: EditText = findViewById(R.id.editTextCalificacion)



                val recetas = BDD.CompbddAplicacion!!.obtenerRecipes()

                if (recetas.isNotEmpty()) {
                    val receta = recetas[posicionItemSeleccionado]

                    // Asignar los valores a las vistas correspondientes
                    editTextNombreReceta.setText(receta.name)
                    editTextCantidadIngredientes.setText(receta.ingredientCount.toString())
                    checkBoxPublica.isChecked = receta.isPublic
                    editTextFechaCreacion.setText(receta.creationDate)
                    editTextCalificacion.setText(receta.rating.toString())
                }

                //boton Guardar
                val buttonGuardar: Button = findViewById(R.id.buttonGuardarReceta)


                buttonGuardar.setOnClickListener {
                    // Obtener los datos de las vistas
                    val nombreReceta = editTextNombreReceta.text.toString()
                    val cantidadIngredientes = editTextCantidadIngredientes.text.toString().toIntOrNull() ?: 0
                    val esPublica = checkBoxPublica.isChecked
                    val fechaCreacion = editTextFechaCreacion.text.toString()
                    val calificacion = editTextCalificacion.text.toString().toDoubleOrNull() ?: 0.0

                    // Crear un objeto Recipe con los datos ingresados
                    val recetaActualizada = Recipe(
                        id = idItemSeleccionado,
                        name = nombreReceta,
                        ingredientCount = cantidadIngredientes,
                        isPublic = esPublica,
                        creationDate = fechaCreacion,
                        rating = calificacion
                    )

                    // Llamar al método para actualizar la receta en la base de datos
                    val exito = BDD.CompbddAplicacion!!.actualizarRecipePorId(recetaActualizada)

                    // Informar al usuario sobre el resultado de la actualización
                    if (exito == true) {
                        Toast.makeText(this, "Receta actualizada con éxito", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error al actualizar la receta", Toast.LENGTH_SHORT).show()
                    }
                }


                return true
            }
            R.id.menu_eliminar ->{

                val exito = BDD.CompbddAplicacion?.eliminarRecipePorId(idItemSeleccionado)
                if (exito == true) {
                    Toast.makeText(this, "Receta eliminada con éxito", Toast.LENGTH_SHORT).show()
                    // Actualiza la interfaz para reflejar la eliminación
                    actualizarListaRecetas()
                } else {
                    Toast.makeText(this, "Error al eliminar la receta", Toast.LENGTH_SHORT).show()
                }

                return true
            }
            R.id.menu_ver -> {

                if(idItemSeleccionado == 0){
                    Log.d("Cero", "${0}")
                }else{
                    val intent = Intent(this, BIngre::class.java)

                    intent.putExtra("REC_ID", idItemSeleccionado)
                    Log.i("enviado", idItemSeleccionado.toString())

                    startActivity(intent)
                }


                return true
            }
            else -> super.onContextItemSelected(item)
        }
    }



    private fun configurarBotonAgregarReceta() {
        val botonAgregarReceta = findViewById<Button>(R.id.btn_anadir_rec_list_view)

        botonAgregarReceta.setOnClickListener {
            // Inflar el nuevo layout (formulario_base.xml)
            val formularioBase = layoutInflater.inflate(R.layout.formulario_base, null)

            // Obtener referencias a las vistas en el nuevo layout
            val editTextNombreReceta: EditText = formularioBase.findViewById(R.id.editTextNombreReceta)
            val editTextCantidadIngredientes: EditText = formularioBase.findViewById(R.id.editTextCantidadIngredientes)
            val checkBoxPublica: CheckBox = formularioBase.findViewById(R.id.checkBoxPublica)
            val editTextFechaCreacion: EditText = formularioBase.findViewById(R.id.editTextFechaCreacion)
            val editTextCalificacion: EditText = formularioBase.findViewById(R.id.editTextCalificacion)

            val buttonGuardarReceta: Button = formularioBase.findViewById(R.id.buttonGuardarReceta)
            buttonGuardarReceta.visibility = View.GONE

            val dialog = AlertDialog.Builder(this)
                .setView(formularioBase)
                .setPositiveButton("Guardar") { _, _ ->
                    // Obtener datos ingresados por el usuario
                    val nombreReceta = editTextNombreReceta.text.toString()
                    val cantidadIngredientes = editTextCantidadIngredientes.text.toString().toIntOrNull() ?: 0
                    val esPublica = checkBoxPublica.isChecked
                    val fechaCreacion = editTextFechaCreacion.text.toString()
                    val calificacion = editTextCalificacion.text.toString().toDoubleOrNull() ?: 0.0f

                    // Crear una nueva receta
                    val nuevaReceta = Recipe(
                        name = nombreReceta,
                        ingredientCount = cantidadIngredientes,
                        isPublic = esPublica,
                        creationDate = fechaCreacion,
                        rating = calificacion as Double
                    )

                    // Insertar la receta en la base de datos
                    val exito = BDD.CompbddAplicacion!!.crearRecipe(nuevaReceta)
                    if (exito) {
                        Toast.makeText(this, "Receta guardada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al guardar la receta", Toast.LENGTH_SHORT).show()
                    }

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()  // Cerrar la actividad actual
                }
                .setNegativeButton("Cancelar", null)
                .create()
            dialog.show()
        }
    }



}

