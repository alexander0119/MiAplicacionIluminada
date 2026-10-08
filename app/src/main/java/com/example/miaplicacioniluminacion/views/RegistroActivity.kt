package com.example.miaplicacioniluminacion.views
import com.example.miaplicacioniluminacion.R


import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// FIREBASE: IMPORTACIONES DE FIREBASE AUTH Y FIRESTORE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegistroActivity : AppCompatActivity() {

    // FIREBASE: DECLARACIÓN DE INSTANCIAS DE FIREBASE
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // FIREBASE: INICIALIZACIÓN DE FIREBASE AUTH Y FIRESTORE
        auth = Firebase.auth
        db = Firebase.firestore
    }

    // Helper para bloquear/desbloquear botones y mostrar/ocultar la barra de carga (loading)
    private fun mostrarEstadoCargando(cargando: Boolean) {
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val btnVolverLogin = findViewById<Button>(R.id.btnVolverLogin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarRegistro)

        btnRegistrar.isEnabled = !cargando
        btnVolverLogin.isEnabled = !cargando
        progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
    }

    // Método ejecutado al hacer clic en el botón Registrarse
    fun onRegistrarClick(view: View) {
        val edtNombre = findViewById<EditText>(R.id.edtNombreRegistro)
        val edtEmail = findViewById<EditText>(R.id.edtEmailRegistro)
        val edtPassword = findViewById<EditText>(R.id.edtPasswordRegistro)
        val edtConfirmarPassword = findViewById<EditText>(R.id.edtConfirmarPassword)

        val nombre = edtNombre.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString()
        val confirmarPassword = edtConfirmarPassword.text.toString()

        var esValido = true

        if (nombre.isEmpty()) {
            edtNombre.error = "Ingresa tu nombre completo"
            esValido = false
        } else {
            edtNombre.error = null
        }

        if (email.isEmpty()) {
            edtEmail.error = "Ingresa tu correo electrónico"
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.error = "Ingresa un correo electrónico válido"
            esValido = false
        } else {
            edtEmail.error = null
        }

        if (password.isEmpty()) {
            edtPassword.error = "Ingresa una contraseña"
            esValido = false
        } else if (password.length < 6) {
            edtPassword.error = "La contraseña debe tener al menos 6 caracteres"
            esValido = false
        } else {
            edtPassword.error = null
        }

        if (confirmarPassword.isEmpty()) {
            edtConfirmarPassword.error = "Confirma tu contraseña"
            esValido = false
        } else if (password != confirmarPassword) {
            edtConfirmarPassword.error = "Las contraseñas no coinciden"
            esValido = false
        } else {
            edtConfirmarPassword.error = null
        }

        if (esValido) {
            // Muestra la barra de carga y bloquea los botones para evitar múltiples clics
            mostrarEstadoCargando(true)

            // FIREBASE 1: CREACIÓN DEL USUARIO EN FIREBASE AUTH
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: ""

                        // FIREBASE 2: CREACIÓN DEL MAPA DE DATOS Y ALMACENAMIENTO EN FIRESTORE
                        val datosUsuario = hashMapOf(
                            "uid" to uid,
                            "nombre" to nombre,
                            "email" to email,
                            "fechaRegistro" to System.currentTimeMillis()
                        )

                        android.util.Log.d("RegistroActivity", "FIREBASE: Intentando guardar en Firestore para UID: $uid")

                        // Guardar en la colección "usuarios" usando el UID como ID del documento
                        db.collection("usuarios").document(uid)
                            .set(datosUsuario)
                            .addOnSuccessListener {
                                android.util.Log.d("RegistroActivity", "FIREBASE: Documento guardado exitosamente en Firestore")
                                Toast.makeText(this, "FIREBASE: Usuario guardado en BD con éxito", Toast.LENGTH_SHORT).show()

                                mostrarEstadoCargando(false)

                                // Navegar a BienvenidaActivity tras registrar exitosamente
                                val intent = Intent(this, BienvenidaActivity::class.java)
                                intent.putExtra("usuario", email)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                mostrarEstadoCargando(false)
                                android.util.Log.e("RegistroActivity", "FIREBASE FIRESTORE Error", e)
                                Toast.makeText(this, "FIREBASE FIRESTORE Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        mostrarEstadoCargando(false)
                        val mensajeError = task.exception?.localizedMessage ?: "Error al registrar usuario"
                        Toast.makeText(this, "FIREBASE AUTH Error: $mensajeError", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // Método ejecutado al hacer clic en el botón Volver
    fun onVolverLoginClick(view: View) {
        finish()
    }
}
