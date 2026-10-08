package com.example.miaplicacioniluminacion.views

// Importa DatePickerDialog para desplegar el selector de fechas tipo calendario
import android.app.DatePickerDialog
// Importa TimePickerDialog para desplegar el selector de hora tipo reloj
import android.app.TimePickerDialog
// Importa Context para acceder a los servicios del sistema Android
import android.content.Context
// Importa ColorStateList para alterar dinámicamente los tonos de fondo de botones
import android.content.res.ColorStateList
// Importa Color para definir colores hexadecimales
import android.graphics.Color
// Importa Sensor para interactuar con los sensores de hardware
import android.hardware.Sensor
// Importa SensorEvent para recibir lecturas de valores del sensor
import android.hardware.SensorEvent
// Importa SensorEventListener para escuchar cambios continuos en los sensores
import android.hardware.SensorEventListener
// Importa SensorManager para registrar y administrar los sensores del dispositivo
import android.hardware.SensorManager
// Importa Build para comprobar la versión del sistema operativo Android
import android.os.Build
// Importa Bundle para manejar el ciclo de vida de la Activity
import android.os.Bundle
// Importa VibrationEffect para generar vibraciones modernas parametrizadas
import android.os.VibrationEffect
// Importa Vibrator para acceder al hardware de vibración
import android.os.Vibrator
// Importa VibratorManager para versiones de Android 12 (API 31) en adelante
import android.os.VibratorManager
// Importa View para recibir el elemento pulsado en el evento onClick
import android.view.View
// Importa Button para los botones de la interfaz
import android.widget.Button
// Importa EditText para capturar entradas de texto del usuario
import android.widget.EditText
// Importa SeekBar para el control deslizante de porcentaje
import android.widget.SeekBar
// Importa TextView para modificar títulos dinámicos en la pantalla
import android.widget.TextView
// Importa Toast para desplegar mensajes breves flotantes en pantalla
import android.widget.Toast
// Importa AppCompatActivity como clase base para la pantalla
import androidx.appcompat.app.AppCompatActivity
// Importa R con los recursos del proyecto
import com.example.miaplicacioniluminacion.R
// Importa el modelo de datos RegistroIluminacion
import com.example.miaplicacioniluminacion.models.RegistroIluminacion
// Importa la instancia de FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestore
// Importa la extensión ktx para inicializar Firestore de forma idiomática
import com.google.firebase.firestore.ktx.firestore
// Importa Firebase ktx
import com.google.firebase.ktx.Firebase
// Importa SimpleDateFormat para formatear la fecha
import java.text.SimpleDateFormat
// Importa Calendar para obtener y configurar fecha y hora
import java.util.Calendar
// Importa Date para obtener la fecha actual del sistema
import java.util.Date
// Importa Locale para la configuración regional y numérica
import java.util.Locale

// Activity encargada de crear o editar un registro de iluminación con sensor automático, confirmación háptica y bloqueo de botón
class FormularioRegistroIluminacionActivity : AppCompatActivity() {

    // Instancia de Firestore que gestionará las operaciones en la base de datos
    private lateinit var db: FirebaseFirestore

    // Referencia al TextView que muestra el título del formulario
    private lateinit var txtTituloFormulario: TextView

    // Referencia al campo de texto de ubicación
    private lateinit var edtUbicacion: EditText

    // Referencia al campo de texto del nivel de luz ambiental
    private lateinit var edtNivelLuzAmbiental: EditText

    // Referencia al botón que alterna el estado del sensor (En vivo / Pausado)
    private lateinit var btnToggleSensor: Button

    // Referencia al TextView que muestra el porcentaje de la intensidad
    private lateinit var txtValorIntensidad: TextView

    // Referencia al SeekBar (control deslizante) de intensidad
    private lateinit var skbIntensidadLuz: SeekBar

    // Referencia al campo de texto de la fecha del registro
    private lateinit var edtFechaRegistro: EditText

    // Referencia al botón de guardar registro
    private lateinit var btnGuardarRegistro: Button

    // Administrador de sensores del sistema
    private var sensorManager: SensorManager? = null

    // Referencia específica al sensor de luz ambiental físico (TYPE_LIGHT)
    private var sensorLuz: Sensor? = null

    // Bandera para controlar si la lectura automática continua está activa
    private var sensorActivo: Boolean = true

    // Variable que almacenará el ID si estamos en modo edición, o null si es nuevo registro
    private var registroIdEditar: String? = null

    // Escucha continuo de eventos del sensor de luz para transmitir valores en tiempo real
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            // Si el sensor está habilitado en vivo y el evento corresponde al sensor de luz
            if (sensorActivo && event != null && event.sensor.type == Sensor.TYPE_LIGHT) {
                val lux = event.values[0]
                // Si el usuario no está escribiendo manualmente en el campo, actualiza en vivo
                if (!edtNivelLuzAmbiental.hasFocus()) {
                    edtNivelLuzAmbiental.setText(String.format(Locale.US, "%.1f", lux))
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    // Método que se ejecuta al iniciar la Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        // Llama al constructor de la clase padre
        super.onCreate(savedInstanceState)
        // Vincula el diseño XML correspondiente
        setContentView(R.layout.activity_formulario_registro_iluminacion)

        // Inicializa la conexión con Firestore
        db = Firebase.firestore

        // Inicializa el SensorManager obteniendo el servicio de hardware del sistema
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Obtiene el sensor de luz ambiental físico si el dispositivo cuenta con él
        sensorLuz = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Enlaza las variables con sus vistas en el XML
        txtTituloFormulario = findViewById(R.id.txtTituloFormulario)
        edtUbicacion = findViewById(R.id.edtUbicacion)
        edtNivelLuzAmbiental = findViewById(R.id.edtNivelLuzAmbiental)
        btnToggleSensor = findViewById(R.id.btnToggleSensor)
        txtValorIntensidad = findViewById(R.id.txtValorIntensidad)
        skbIntensidadLuz = findViewById(R.id.skbIntensidadLuz)
        edtFechaRegistro = findViewById(R.id.edtFechaRegistro)
        btnGuardarRegistro = findViewById(R.id.btnGuardarRegistro)

        // Configura el listener del SeekBar para actualizar el TextView en tiempo real
        skbIntensidadLuz.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                txtValorIntensidad.text = "Intensidad de luz: $progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Asigna el evento de clic al campo de fecha para abrir el calendario y reloj
        edtFechaRegistro.setOnClickListener {
            mostrarSelectorFechaHora()
        }

        // Recupera el ID del registro si fue enviado a través del Intent
        registroIdEditar = intent.getStringExtra("registroId")

        // Determina si estamos editando un registro existente o creando uno nuevo
        if (registroIdEditar != null) {
            // MODO EDICIÓN: Pausa la lectura automática para no sobreescribir el dato guardado
            sensorActivo = false
            btnToggleSensor.text = "Pausado ⏸"
            btnToggleSensor.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#757575"))

            txtTituloFormulario.text = "Editar Registro de Iluminación"
            edtUbicacion.setText(intent.getStringExtra("registroUbicacion"))
            edtNivelLuzAmbiental.setText(
                intent.getDoubleExtra("registroNivelLuz", 0.0).toString()
            )
            val intensidad = intent.getIntExtra("registroIntensidad", 0)
            skbIntensidadLuz.progress = intensidad
            txtValorIntensidad.text = "Intensidad de luz: $intensidad%"
            edtFechaRegistro.setText(intent.getStringExtra("registroFecha"))
        } else {
            // MODO CREACIÓN: El estado inicial del sensor respeta la preferencia guardada
            val prefs =
                getSharedPreferences(PreferenciasActivity.PREFS_NAME, Context.MODE_PRIVATE)
            val ajusteAutomatico =
                prefs.getBoolean(PreferenciasActivity.KEY_AJUSTE_AUTOMATICO, false)

            if (ajusteAutomatico) {
                // Preferencia activada → sensor arranca en vivo automáticamente
                sensorActivo = true
                btnToggleSensor.text = "En vivo 🟢"
                btnToggleSensor.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#009688"))
            } else {
                // Preferencia desactivada → sensor arranca pausado, el usuario lo activa a mano
                sensorActivo = false
                btnToggleSensor.text = "Pausado ⏸"
                btnToggleSensor.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#757575"))
            }

            txtTituloFormulario.text = "Nuevo Registro de Iluminación"
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            edtFechaRegistro.setText(formatoFecha.format(Date()))

            // Pre-rellena la ubicación con la preferencia guardada (si el usuario ya eligió una)
            val ubicacionGuardada =
                prefs.getString(PreferenciasActivity.KEY_UBICACION_TEXTO, "")
            if (!ubicacionGuardada.isNullOrEmpty() && ubicacionGuardada != "Seleccionar ubicación") {
                edtUbicacion.setText(ubicacionGuardada)
            }
        }
    }

    // Al reanudar la Activity, registra el sensor para lectura automática si está activo
    override fun onResume() {
        super.onResume()
        if (sensorLuz != null && sensorActivo) {
            sensorManager?.registerListener(
                sensorListener,
                sensorLuz,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    // Al pausar o salir de la Activity, desuscribe el sensor para optimizar la batería
    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(sensorListener)
    }

    // Alterna entre lectura automática en vivo y manual al tocar el botón del sensor
    fun onToggleSensorClick(view: View) {
        if (sensorLuz == null) {
            Toast.makeText(
                this,
                "Este dispositivo no cuenta con sensor de luz físico",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        sensorActivo = !sensorActivo
        if (sensorActivo) {
            // Activa el sensor continuo
            sensorManager?.registerListener(
                sensorListener,
                sensorLuz,
                SensorManager.SENSOR_DELAY_UI
            )
            btnToggleSensor.text = "En vivo 🟢"
            btnToggleSensor.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#009688"))
            Toast.makeText(this, "Sensor automático activado", Toast.LENGTH_SHORT).show()
        } else {
            // Pausa el sensor
            sensorManager?.unregisterListener(sensorListener)
            btnToggleSensor.text = "Pausado ⏸"
            btnToggleSensor.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#757575"))
            Toast.makeText(this, "Sensor automático pausado", Toast.LENGTH_SHORT).show()
        }
    }

    // Despliega un DatePickerDialog seguido de un TimePickerDialog para seleccionar fecha y hora con calendario y reloj
    private fun mostrarSelectorFechaHora() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        // Abre el selector de fecha (calendario)
        val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            val hora = calendario.get(Calendar.HOUR_OF_DAY)
            val minuto = calendario.get(Calendar.MINUTE)

            // Luego de elegir la fecha, abre el selector de hora (reloj)
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                val fechaSeleccionada = Calendar.getInstance()
                fechaSeleccionada.set(year, monthOfYear, dayOfMonth, hourOfDay, minute)
                val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                edtFechaRegistro.setText(formato.format(fechaSeleccionada.time))
            }, hora, minuto, true)

            timePickerDialog.show()
        }, anio, mes, dia)

        datePickerDialog.show()
    }

    // Ejecuta una breve vibración de confirmación háptica al guardar o actualizar un registro
    private fun ejecutarVibracion() {
        try {
            // Obtiene el servicio de vibración según la versión de Android
            val vibrador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            // Emite un pulso de vibración de 150 milisegundos
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrador?.vibrate(
                    VibrationEffect.createOneShot(
                        150,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrador?.vibrate(150)
            }
        } catch (e: Exception) {
            // Captura silenciosa en caso de que el dispositivo carezca de motor vibrador
        }
    }

    // Método invocado al presionar el botón "Guardar Registro" definido en el XML
    fun onGuardarRegistroClick(view: View) {
        // Extrae el texto de cada campo
        val ubicacion = edtUbicacion.text.toString().trim()
        val nivelLuzStr = edtNivelLuzAmbiental.text.toString().trim()
        val intensidad = skbIntensidadLuz.progress
        val fecha = edtFechaRegistro.text.toString().trim()

        // Valida que los campos obligatorios contengan información
        if (ubicacion.isEmpty() || nivelLuzStr.isEmpty() || fecha.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Convierte el nivel de luz ambiental a Double
        val nivelLuz = nivelLuzStr.toDoubleOrNull()
        if (nivelLuz == null) {
            Toast.makeText(
                this,
                "El nivel de luz ambiental debe ser un número válido",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Deshabilita el botón temporalmente y actualiza su texto para evitar doble clic accidental
        btnGuardarRegistro.isEnabled = false
        btnGuardarRegistro.text = "Guardando..."

        // Si registroIdEditar no es nulo, actualizamos el documento existente
        if (registroIdEditar != null) {
            // Instancia el objeto con los datos modificados manteniendo su ID original
            val registroActualizado = RegistroIluminacion(
                id = registroIdEditar,
                ubicacion = ubicacion,
                nivelLuzAmbiental = nivelLuz,
                intensidadLuz = intensidad,
                fechaRegistro = fecha
            )

            // Sobrescribe el documento en la colección "registros_iluminacion"
            db.collection("registros_iluminacion").document(registroIdEditar!!)
                .set(registroActualizado)
                .addOnSuccessListener {
                    // Dispara la respuesta háptica de vibración
                    ejecutarVibracion()
                    Toast.makeText(this, "Registro actualizado con éxito", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
                .addOnFailureListener { e ->
                    // Restablece el botón en caso de error para permitir reintentar
                    btnGuardarRegistro.isEnabled = true
                    btnGuardarRegistro.text = "Guardar Registro"
                    Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
        } else {
            // MODO CREACIÓN: Instancia un nuevo objeto sin ID para que Firestore lo asigne automáticamente
            val nuevoRegistro = RegistroIluminacion(
                ubicacion = ubicacion,
                nivelLuzAmbiental = nivelLuz,
                intensidadLuz = intensidad,
                fechaRegistro = fecha
            )

            // Agrega un nuevo documento a la colección "registros_iluminacion"
            db.collection("registros_iluminacion")
                .add(nuevoRegistro)
                .addOnSuccessListener {
                    // Dispara la respuesta háptica de vibración
                    ejecutarVibracion()
                    Toast.makeText(this, "Registro guardado con éxito", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
                .addOnFailureListener { e ->
                    // Restablece el botón en caso de error para permitir reintentar
                    btnGuardarRegistro.isEnabled = true
                    btnGuardarRegistro.text = "Guardar Registro"
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
        }
    }
}
