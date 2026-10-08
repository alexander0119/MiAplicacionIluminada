package com.example.miaplicacioniluminacion.views

// Importa AlertDialog para mostrar cuadros de diálogo de confirmación y selección
import android.app.AlertDialog
// Importa Intent para navegar entre actividades y compartir información vía apps externas
import android.content.Intent
// Importa Bundle para el ciclo de vida de la actividad
import android.os.Bundle
// Importa TextWatcher para monitorear cambios en tiempo real en la caja de búsqueda
import android.text.Editable
import android.text.TextWatcher
// Importa Log para registrar mensajes de depuración y errores
import android.util.Log
// Importa View para manipular la visibilidad de elementos visuales
import android.view.View
// Importa EditText para el campo de búsqueda de texto
import android.widget.EditText
// Importa ImageButton para los botones de la barra superior
import android.widget.ImageButton
// Importa TextView para el mensaje de lista vacía
import android.widget.TextView
// Importa Toast para notificaciones breves
import android.widget.Toast
// Importa AppCompatActivity como clase base de la actividad
import androidx.appcompat.app.AppCompatActivity
// Importa ItemTouchHelper para implementar el gesto de deslizar para borrar (Swipe-to-delete)
import androidx.recyclerview.widget.ItemTouchHelper
// Importa LinearLayoutManager para ordenar los ítems en formato vertical
import androidx.recyclerview.widget.LinearLayoutManager
// Importa RecyclerView para gestionar la lista eficiente de elementos
import androidx.recyclerview.widget.RecyclerView
// Importa el recurso R del proyecto
import com.example.miaplicacioniluminacion.R
// Importa el adaptador personalizado de registros de iluminación
import com.example.miaplicacioniluminacion.adapters.RegistroIluminacionAdapter
// Importa el modelo de datos RegistroIluminacion
import com.example.miaplicacioniluminacion.models.RegistroIluminacion
// Importa el botón flotante FloatingActionButton de Material Design
import com.google.android.material.floatingactionbutton.FloatingActionButton
// Importa FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestore
// Importa la extensión ktx para inicializar Firestore
import com.google.firebase.firestore.ktx.firestore
// Importa Firebase ktx
import com.google.firebase.ktx.Firebase

// Activity que despliega el listado de registros con buscador, ordenamiento, swipe-to-delete y compartir
class ListaRegistroIluminacionActivity : AppCompatActivity() {

    // Variable para la instancia de Firestore
    private lateinit var db: FirebaseFirestore
    // Adaptador que conecta los datos con el RecyclerView
    private lateinit var adapter: RegistroIluminacionAdapter
    // Referencia al RecyclerView en el diseño XML
    private lateinit var rvRegistrosIluminacion: RecyclerView
    // Referencia al campo de búsqueda en el diseño XML
    private lateinit var edtBuscarRegistro: EditText
    // Referencia al botón para limpiar el texto de búsqueda
    private lateinit var btnLimpiarBusqueda: ImageButton
    // Referencia al botón para ordenar los registros
    private lateinit var btnOrdenarRegistros: ImageButton
    // Referencia al botón para compartir el reporte
    private lateinit var btnCompartirReporte: ImageButton
    // Referencia al TextView que se muestra cuando no hay resultados
    private lateinit var txtSinResultados: TextView

    // Lista en memoria que conserva todos los registros descargados desde Firestore
    private var listaCompleta: List<RegistroIluminacion> = listOf()

    // Variable para controlar el criterio de ordenamiento activo (0: Más reciente por defecto)
    private var criterioOrdenSeleccionado: Int = 0

    // Opciones disponibles en el diálogo de ordenamiento
    private val opcionesOrdenamiento = arrayOf(
        "Fecha (Más reciente primero)",
        "Fecha (Más antiguo primero)",
        "Mayor intensidad de luz",
        "Menor intensidad de luz",
        "Mayor luz ambiental (lx)",
        "Ubicación (A - Z)"
    )

    // Método que se ejecuta al iniciar la Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        // Ejecuta la inicialización de la superclase
        super.onCreate(savedInstanceState)
        // Vincula el diseño XML activity_lista_registro_iluminacion
        setContentView(R.layout.activity_lista_registro_iluminacion)

        // Inicializa Firestore
        db = Firebase.firestore

        // Configura el adaptador con callbacks de editar, eliminar y compartir
        adapter = RegistroIluminacionAdapter(
            onEditarClick = { registroAEditar ->
                // Crea el intent para navegar al formulario en modo edición
                val intent = Intent(this, FormularioRegistroIluminacionActivity::class.java)
                intent.putExtra("registroId", registroAEditar.id)
                intent.putExtra("registroUbicacion", registroAEditar.ubicacion)
                intent.putExtra("registroNivelLuz", registroAEditar.nivelLuzAmbiental)
                intent.putExtra("registroIntensidad", registroAEditar.intensidadLuz)
                intent.putExtra("registroFecha", registroAEditar.fechaRegistro)
                startActivity(intent)
            },
            onEliminarClick = { registroAEliminar ->
                // Muestra la ventana de diálogo para confirmar la eliminación
                mostrarDialogoConfirmacion(registroAEliminar)
            },
            onCompartirClick = { registroACompartir ->
                // Comparte la medición individual
                compartirRegistroIndividual(registroACompartir)
            }
        )

        // Busca el RecyclerView por su ID
        rvRegistrosIluminacion = findViewById(R.id.rvRegistrosIluminacion)
        // Establece la disposición lineal vertical para los elementos
        rvRegistrosIluminacion.layoutManager = LinearLayoutManager(this)
        // Asigna el adaptador configurado al RecyclerView
        rvRegistrosIluminacion.adapter = adapter

        // Configura el gesto de Swipe-to-delete (deslizar a la izquierda o derecha para eliminar)
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicion = viewHolder.adapterPosition
                val registro = adapter.obtenerRegistroEn(posicion)
                mostrarDialogoConfirmacion(registro, posicion)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(rvRegistrosIluminacion)

        // Enlaza el campo de búsqueda por su ID
        edtBuscarRegistro = findViewById(R.id.edtBuscarRegistro)
        // Enlaza el botón para limpiar la búsqueda
        btnLimpiarBusqueda = findViewById(R.id.btnLimpiarBusqueda)
        // Enlaza el botón para ordenar registros
        btnOrdenarRegistros = findViewById(R.id.btnOrdenarRegistros)
        // Enlaza el botón para compartir reporte
        btnCompartirReporte = findViewById(R.id.btnCompartirReporte)
        // Enlaza el TextView para estado vacío
        txtSinResultados = findViewById(R.id.txtSinResultados)

        // Configura el botón para borrar el texto y restablecer la lista completa
        btnLimpiarBusqueda.setOnClickListener {
            edtBuscarRegistro.setText("")
        }

        // Configura el botón para desplegar el selector de criterios de ordenamiento
        btnOrdenarRegistros.setOnClickListener {
            mostrarDialogoOrdenamiento()
        }

        // Configura el botón para compartir el reporte general de mediciones
        btnCompartirReporte.setOnClickListener {
            compartirReporteGeneral()
        }

        // Configura un TextWatcher para filtrar la lista instantáneamente mientras el usuario escribe
        edtBuscarRegistro.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filtra y ordena la lista según el texto ingresado
                filtrarYOrdenarRegistros(s.toString())
                // Muestra el botón de limpiar solo cuando haya texto escrito
                btnLimpiarBusqueda.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Inicia la sincronización en tiempo real con Firestore
        escucharRegistros()

        // Configura el FloatingActionButton para agregar un nuevo registro
        val fabAgregar = findViewById<FloatingActionButton>(R.id.fabAgregarRegistro)
        fabAgregar.setOnClickListener {
            val intent = Intent(this, FormularioRegistroIluminacionActivity::class.java)
            startActivity(intent)
        }
    }

    // Escucha cambios en tiempo real en la colección "registros_iluminacion"
    private fun escucharRegistros() {
        db.collection("registros_iluminacion").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ListaRegistros", "Error al escuchar registros de iluminación.", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                listaCompleta = snapshot.toObjects(RegistroIluminacion::class.java)
                Log.d("ListaRegistros", "Registros recuperados: ${listaCompleta.size}")
                filtrarYOrdenarRegistros(edtBuscarRegistro.text.toString())
            }
        }
    }

    // Despliega un diálogo de selección múltiple simple con las opciones de ordenamiento
    private fun mostrarDialogoOrdenamiento() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ordenar registros por")
        builder.setSingleChoiceItems(opcionesOrdenamiento, criterioOrdenSeleccionado) { dialog, opcionElegida ->
            criterioOrdenSeleccionado = opcionElegida
            filtrarYOrdenarRegistros(edtBuscarRegistro.text.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    // Filtra y ordena la lista en memoria según el término de búsqueda y el criterio seleccionado
    private fun filtrarYOrdenarRegistros(criterio: String) {
        val query = criterio.trim()

        val listaFiltrada = if (query.isEmpty()) {
            listaCompleta
        } else {
            listaCompleta.filter { registro ->
                registro.ubicacion.contains(query, ignoreCase = true) ||
                registro.fechaRegistro.contains(query, ignoreCase = true)
            }
        }

        val listaOrdenada = when (criterioOrdenSeleccionado) {
            0 -> listaFiltrada.sortedByDescending { it.fechaRegistro }
            1 -> listaFiltrada.sortedBy { it.fechaRegistro }
            2 -> listaFiltrada.sortedByDescending { it.intensidadLuz }
            3 -> listaFiltrada.sortedBy { it.intensidadLuz }
            4 -> listaFiltrada.sortedByDescending { it.nivelLuzAmbiental }
            5 -> listaFiltrada.sortedBy { it.ubicacion.lowercase() }
            else -> listaFiltrada
        }

        txtSinResultados.visibility = if (listaOrdenada.isEmpty()) View.VISIBLE else View.GONE
        adapter.actualizarLista(listaOrdenada)
    }

    // Despliega un AlertDialog para confirmar eliminación (soporta restauración en caso de Swipe)
    private fun mostrarDialogoConfirmacion(registro: RegistroIluminacion, posicionSwipe: Int? = null) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar Registro")
        builder.setMessage("¿Estás seguro de que deseas eliminar el registro de '${registro.ubicacion}'?")

        builder.setPositiveButton("Sí, eliminar") { dialog, _ ->
            eliminarRegistro(registro)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            // Si el usuario cancela, restablece la fila en el RecyclerView para que regrese a su sitio
            if (posicionSwipe != null) {
                adapter.notifyItemChanged(posicionSwipe)
            }
            dialog.dismiss()
        }

        builder.setOnCancelListener {
            if (posicionSwipe != null) {
                adapter.notifyItemChanged(posicionSwipe)
            }
        }

        builder.create().show()
    }

    // Elimina el documento de Firestore utilizando su identificador
    private fun eliminarRegistro(registro: RegistroIluminacion) {
        val id = registro.id ?: return
        db.collection("registros_iluminacion").document(id)
            .delete()
            .addOnSuccessListener {
                Log.d("ListaRegistros", "Registro eliminado exitosamente")
            }
            .addOnFailureListener { e ->
                Log.w("ListaRegistros", "Error al eliminar el registro", e)
            }
    }

    // Comparte una medición individual formateada hacia otras apps (WhatsApp, Correo, etc.)
    private fun compartirRegistroIndividual(registro: RegistroIluminacion) {
        val mensaje = """
            💡 Registro de Iluminación
            📍 Ubicación: ${registro.ubicacion}
            ☀️ Luz Ambiental: ${registro.nivelLuzAmbiental} lx
            🔆 Intensidad: ${registro.intensidadLuz}%
            📅 Fecha: ${registro.fechaRegistro}
        """.trimIndent()

        val intentCompartir = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, mensaje)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intentCompartir, "Compartir registro"))
    }

    // Genera y comparte un reporte de texto con todos los registros actuales
    private fun compartirReporteGeneral() {
        if (listaCompleta.isEmpty()) {
            Toast.makeText(this, "No hay registros disponibles para compartir", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.append("📋 REPORTE DE MEDICIONES DE ILUMINACIÓN\n")
        sb.append("Total de registros: ${listaCompleta.size}\n\n")

        listaCompleta.forEachIndexed { indice, reg ->
            sb.append("${indice + 1}. [${reg.fechaRegistro}] ${reg.ubicacion}\n")
            sb.append("   - Luz: ${reg.nivelLuzAmbiental} lx | Intensidad: ${reg.intensidadLuz}%\n\n")
        }

        val intentCompartir = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intentCompartir, "Compartir reporte de mediciones"))
    }
}
