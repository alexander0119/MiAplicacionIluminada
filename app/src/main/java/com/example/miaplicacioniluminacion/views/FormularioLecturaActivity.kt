package com.example.miaplicacioniluminacion.views

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.miaplicacioniluminacion.R
import com.example.miaplicacioniluminacion.models.Lectura
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FormularioLecturaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var txtTituloFormulario: TextView
    private lateinit var edtDescripcion: EditText
    private lateinit var edtValor: EditText

    // Variable para saber si estamos editando (guardará el ID) o creando (será null)
    private var lecturaIdEditar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_lectura)

        db = Firebase.firestore

        txtTituloFormulario = findViewById(R.id.txtTituloFormulario)
        edtDescripcion = findViewById(R.id.edtDescripcion)
        edtValor = findViewById(R.id.edtValor)

        // Revisamos si el Intent trae datos adjuntos (lo que significa que venimos a Editar)
        lecturaIdEditar = intent.getStringExtra("lecturaId")

        if (lecturaIdEditar != null) {
            // MODO EDITAR
            txtTituloFormulario.text = "Editar Lectura"
            // Llenamos los campos con los datos actuales que nos enviaron
            edtDescripcion.setText(intent.getStringExtra("lecturaDescripcion"))
            val valor = intent.getDoubleExtra("lecturaValor", 0.0)
            edtValor.setText(valor.toString())
        } else {
            // MODO CREAR
            txtTituloFormulario.text = "Nueva Lectura"
        }
    }

    fun onGuardarLecturaClick(view: View) {
        val descripcion = edtDescripcion.text.toString()
        val valorString = edtValor.text.toString()

        if (descripcion.isEmpty() || valorString.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val valorDouble = valorString.toDoubleOrNull()
        if (valorDouble == null) {
            Toast.makeText(this, "El valor ingresado no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (lecturaIdEditar != null) {
            // MODO EDITAR: Usamos el ID existente para SOBREESCRIBIR el documento en Firebase
            val lecturaActualizada = Lectura(id = lecturaIdEditar!!, descripcion = descripcion, valor = valorDouble)
            
            db.collection("registros_iluminacion").document(lecturaIdEditar!!)
                .set(lecturaActualizada) // .set() reemplaza los datos del documento
                .addOnSuccessListener {
                    Toast.makeText(this, "Lectura actualizada", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // MODO CREAR: No enviamos ID, usamos .add() para que Firestore lo cree
            val nuevaLectura = Lectura(descripcion = descripcion, valor = valorDouble)

            db.collection("registros_iluminacion")
                .add(nuevaLectura)
                .addOnSuccessListener {
                    Toast.makeText(this, "Lectura guardada", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
