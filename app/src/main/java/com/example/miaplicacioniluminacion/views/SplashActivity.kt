package com.example.miaplicacioniluminacion.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.miaplicacioniluminacion.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Pantalla de carga (Splash Screen) que se muestra durante 2 segundos al abrir la app
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Deshabilita el botón/gesto Atrás mientras la pantalla de carga está visible
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Bloquea el retroceso intencionalmente durante la carga
            }
        })

        // Espera exactamente 2 segundos y transiciona con animación suave
        Handler(Looper.getMainLooper()).postDelayed({
            // Verifica si el usuario ya tiene una sesión iniciada en Firebase
            val auth = Firebase.auth
            val usuarioActual = auth.currentUser

            val intent = if (usuarioActual != null) {
                // Sesión activa: salta directamente a la pantalla de bienvenida
                Intent(this, BienvenidaActivity::class.java).apply {
                    putExtra("usuario", usuarioActual.email ?: "Usuario")
                }
            } else {
                // Sin sesión: navega a la pantalla de login (MainActivity)
                Intent(this, MainActivity::class.java)
            }

            startActivity(intent)

            // Animación de transición suave (fade in / fade out)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            // Finaliza SplashActivity para que no quede en la pila de navegación
            finish()
        }, 2000)
    }
}
