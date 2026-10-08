package com.example.miaplicacioniluminacion.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.miaplicacioniluminacion.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Declaración de la clase para la pantalla principal posterior al inicio de sesión (Bienvenida)
class BienvenidaActivity : AppCompatActivity() {

    // Variable a nivel de clase para almacenar el nombre de usuario recibido y reutilizarlo
    private var nombreUsuario: String? = null

    // Referencia al TextView indicador del estado del ajuste automático de luz
    private lateinit var txtEstadoAjuste: TextView

    // Referencia al TextView indicador de la ubicación preferida guardada
    private lateinit var txtEstadoUbicacion: TextView

    // Marca temporal para controlar la confirmación de salida mediante doble pulsación
    private var ultimoTiempoAtras: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)
        txtEstadoAjuste = findViewById(R.id.txtEstadoAjuste)
        txtEstadoUbicacion = findViewById(R.id.txtEstadoUbicacion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Lee el dato tipo String enviado desde MainActivity o SplashActivity
        nombreUsuario = intent.getStringExtra("usuario")
        val txtBienvenida = findViewById<TextView>(R.id.txtBienvenida)
        txtBienvenida.text = "Bienvenido, $nombreUsuario"

        // Manejador de doble pulsación para evitar salir accidentalmente de la app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val tiempoActual = System.currentTimeMillis()
                if (tiempoActual - ultimoTiempoAtras < 2000) {
                    isEnabled = false
                    finishAffinity() // Cierra completamente la aplicación
                } else {
                    ultimoTiempoAtras = tiempoActual
                    Toast.makeText(
                        this@BienvenidaActivity,
                        "Presiona atrás nuevamente para salir",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    // Se ejecuta cada vez que la pantalla vuelve a primer plano (por ej. al regresar de Preferencias)
    override fun onResume() {
        super.onResume()
        actualizarIndicadores()
    }

    // Lee las preferencias guardadas desde SharedPreferences y actualiza ambos indicadores
    private fun actualizarIndicadores() {
        val prefs = getSharedPreferences(PreferenciasActivity.PREFS_NAME, Context.MODE_PRIVATE)

        // ── Indicador de ajuste automático ───────────────────────────────────
        val ajusteActivo = prefs.getBoolean(PreferenciasActivity.KEY_AJUSTE_AUTOMATICO, false)
        if (ajusteActivo) {
            txtEstadoAjuste.text = "⚙️ Ajuste automático: ACTIVADO"
            txtEstadoAjuste.setTextColor(android.graphics.Color.parseColor("#009688"))
        } else {
            txtEstadoAjuste.text = "⚙️ Ajuste automático: DESACTIVADO"
            txtEstadoAjuste.setTextColor(android.graphics.Color.parseColor("#757575"))
        }

        // ── Indicador de ubicación preferida guardada ────────────────────────
        val ubicacionGuardada = prefs.getString(PreferenciasActivity.KEY_UBICACION_TEXTO, "")
        if (!ubicacionGuardada.isNullOrEmpty() && ubicacionGuardada != "Seleccionar ubicación") {
            txtEstadoUbicacion.text = "📍 Ubicación guardada: $ubicacionGuardada"
            txtEstadoUbicacion.setTextColor(android.graphics.Color.parseColor("#009688"))
        } else {
            txtEstadoUbicacion.text = "📍 Ubicación guardada: Sin definir"
            txtEstadoUbicacion.setTextColor(android.graphics.Color.parseColor("#757575"))
        }
    }

    // Navega hacia la pantalla de Preferencias
    fun onPreferenciasClick(view: View) {
        val intent = Intent(this, PreferenciasActivity::class.java)
        intent.putExtra("usuario", nombreUsuario)
        startActivity(intent)
    }

    // Navega hacia la pantalla de Lecturas
    fun onVerLecturasClick(view: View) {
        val intent = Intent(this, ListaActivity::class.java)
        startActivity(intent)
    }

    // Navega hacia la pantalla de Registros de Iluminación
    fun onVerRegistrosIluminacionClick(view: View) {
        val intent = Intent(this, ListaRegistroIluminacionActivity::class.java)
        startActivity(intent)
    }

    // Botón para cerrar sesión con diálogo de confirmación y redirección al Login
    fun onCerrarSesionClick(view: View) {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí, salir") { _, _ ->
                // Cierra la sesión activa en Firebase
                Firebase.auth.signOut()

                // Redirige al login limpiando el stack de navegación
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
