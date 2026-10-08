package com.example.miaplicacioniluminacion.views

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.miaplicacioniluminacion.R
import com.google.android.material.materialswitch.MaterialSwitch

class PreferenciasActivity : AppCompatActivity() {

    // Claves compartidas con otras Activities para leer las preferencias del usuario
    companion object {
        const val PREFS_NAME             = "preferencias_luz"
        const val KEY_AJUSTE_AUTOMATICO  = "ajuste_automatico_luz"
        const val KEY_UBICACION_POSICION = "ubicacion_posicion"
        const val KEY_UBICACION_TEXTO    = "ubicacion_texto"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preferencias)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // ── Referencias a los componentes de la interfaz ──────────────────────
        val swAjuste = findViewById<MaterialSwitch>(R.id.swNotificaciones)
        val spUbicacion = findViewById<Spinner>(R.id.spIdioma)
        val btnRestablecer = findViewById<Button>(R.id.btnRestablecerPreferencias)

        // ── Restaurar valores previamente guardados ───────────────────────────
        val guardadoAjuste = prefs.getBoolean(KEY_AJUSTE_AUTOMATICO, false)
        val guardadoPosicion = prefs.getInt(KEY_UBICACION_POSICION, 0)

        swAjuste.isChecked = guardadoAjuste
        spUbicacion.setSelection(guardadoPosicion)

        // ── 1. Guardado automático del Switch con respuesta háptica ───────────
        swAjuste.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AJUSTE_AUTOMATICO, isChecked).apply()
            ejecutarVibracion(50)
            Toast.makeText(this, "Ajuste automático guardado", Toast.LENGTH_SHORT).show()
        }

        // ── 2. Guardado automático del Spinner con respuesta háptica ───────────
        var inicializado = false
        var restableciendo = false

        spUbicacion.post {
            inicializado = true
        }

        spUbicacion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!inicializado || restableciendo) return

                if (position > 0) {
                    val ubicacionSeleccionada = parent?.getItemAtPosition(position).toString()
                    prefs.edit()
                        .putInt(KEY_UBICACION_POSICION, position)
                        .putString(KEY_UBICACION_TEXTO, ubicacionSeleccionada)
                        .apply()
                    ejecutarVibracion(70)
                    Toast.makeText(
                        this@PreferenciasActivity,
                        "Ubicación guardada automáticamente: $ubicacionSeleccionada",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Si el usuario regresa a la opción por defecto (posición 0)
                    prefs.edit()
                        .putInt(KEY_UBICACION_POSICION, 0)
                        .putString(KEY_UBICACION_TEXTO, "")
                        .apply()
                    ejecutarVibracion(100)
                    Toast.makeText(
                        this@PreferenciasActivity,
                        "⚠️ Advertencia: Debes seleccionar una ubicación válida",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ── 3. Botón Restablecer valores por defecto con confirmación y vibración
        btnRestablecer.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Restablecer preferencias")
                .setMessage("¿Estás seguro? Esta acción borrará todas tus preferencias guardadas y no se puede deshacer.")
                .setPositiveButton("Sí, restablecer") { dialog, _ ->
                    restableciendo = true
                    // Borra todas las preferencias
                    prefs.edit().clear().apply()
                    // Regresa controles a sus estados iniciales
                    swAjuste.isChecked = false
                    spUbicacion.setSelection(0)
                    spUbicacion.post { restableciendo = false }

                    ejecutarVibracion(150)
                    Toast.makeText(this, "🔄 Valores restablecidos por defecto", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // ── 4. Validación estricta: NO permitir salir sin ubicación seleccionada ─
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (spUbicacion.selectedItemPosition == 0) {
                    ejecutarVibracion(120)
                    Toast.makeText(
                        this@PreferenciasActivity,
                        "⚠️ Advertencia: No puedes salir sin seleccionar una ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val spUbicacion = findViewById<Spinner>(R.id.spIdioma)
            if (spUbicacion.selectedItemPosition == 0) {
                ejecutarVibracion(120)
                Toast.makeText(
                    this,
                    "⚠️ Advertencia: No puedes salir sin seleccionar una ubicación",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Emite una vibración / respuesta háptica parametrizada por duración
    private fun ejecutarVibracion(duracionMs: Long = 70) {
        try {
            val vibrador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrador?.vibrate(
                    VibrationEffect.createOneShot(duracionMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrador?.vibrate(duracionMs)
            }
        } catch (_: Exception) {
            // Manejo silencioso en dispositivos que no posean vibrador físico
        }
    }
}