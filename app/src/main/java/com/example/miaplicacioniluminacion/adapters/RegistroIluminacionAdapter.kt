package com.example.miaplicacioniluminacion.adapters

// Importa LayoutInflater para convertir el archivo XML del ítem en vistas utilizables
import android.view.LayoutInflater
// Importa View, la clase base para los elementos visuales de la interfaz
import android.view.View
// Importa ViewGroup, el contenedor que alberga las vistas de cada elemento
import android.view.ViewGroup
// Importa ImageButton para los botones de eliminar y compartir
import android.widget.ImageButton
// Importa ProgressBar para la representación gráfica del nivel de intensidad
import android.widget.ProgressBar
// Importa TextView para mostrar textos en cada fila
import android.widget.TextView
// Importa RecyclerView, el componente optimizado para listas reciclables
import androidx.recyclerview.widget.RecyclerView
// Importa los recursos de la aplicación (R)
import com.example.miaplicacioniluminacion.R
// Importa el modelo de datos RegistroIluminacion
import com.example.miaplicacioniluminacion.models.RegistroIluminacion

// Clase adaptadora encargada de conectar la lista de registros con el RecyclerView
// Recibe la lista inicial y funciones lambda para editar, eliminar y compartir
class RegistroIluminacionAdapter(
    private var listaRegistros: List<RegistroIluminacion> = listOf(),
    private val onEditarClick: (RegistroIluminacion) -> Unit,
    private val onEliminarClick: (RegistroIluminacion) -> Unit,
    private val onCompartirClick: (RegistroIluminacion) -> Unit
) : RecyclerView.Adapter<RegistroIluminacionAdapter.RegistroViewHolder>() {

    // Método que crea una nueva vista para una fila inflando el archivo item_registro_iluminacion.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registro_iluminacion, parent, false)
        return RegistroViewHolder(view)
    }

    // Método que vincula los datos de un registro específico en la posición dada con el ViewHolder
    override fun onBindViewHolder(holder: RegistroViewHolder, position: Int) {
        val registro = listaRegistros[position]
        holder.bind(registro, onEditarClick, onEliminarClick, onCompartirClick)
    }

    // Devuelve la cantidad total de elementos contenidos en la lista
    override fun getItemCount(): Int {
        return listaRegistros.size
    }

    // Actualiza la lista de datos y notifica al RecyclerView para que redibuje los elementos
    fun actualizarLista(nuevaLista: List<RegistroIluminacion>) {
        listaRegistros = nuevaLista
        notifyDataSetChanged()
    }

    // Permite obtener el registro ubicado en una posición específica (útil para Swipe-to-delete)
    fun obtenerRegistroEn(posicion: Int): RegistroIluminacion {
        return listaRegistros[posicion]
    }

    // ViewHolder que almacena en caché las referencias a los componentes visuales de cada fila
    class RegistroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Enlaza el TextView de la ubicación
        private val txtUbicacion: TextView = itemView.findViewById(R.id.txtUbicacion)
        // Enlaza el TextView del nivel de luz ambiental
        private val txtNivelLuzAmbiental: TextView = itemView.findViewById(R.id.txtNivelLuzAmbiental)
        // Enlaza el TextView de la intensidad de luz
        private val txtIntensidadLuz: TextView = itemView.findViewById(R.id.txtIntensidadLuz)
        // Enlaza el ProgressBar horizontal de la intensidad de luz
        private val pbIntensidadLuz: ProgressBar = itemView.findViewById(R.id.pbIntensidadLuz)
        // Enlaza el TextView de la fecha del registro
        private val txtFechaRegistro: TextView = itemView.findViewById(R.id.txtFechaRegistro)
        // Enlaza el ImageButton para compartir la medición
        private val btnCompartirRegistro: ImageButton = itemView.findViewById(R.id.btnCompartirRegistro)
        // Enlaza el ImageButton para eliminar el ítem
        private val btnEliminarRegistro: ImageButton = itemView.findViewById(R.id.btnEliminarRegistro)

        // Asigna los valores del objeto RegistroIluminacion a los campos de la interfaz y configura los eventos de clic
        fun bind(
            registro: RegistroIluminacion,
            onEditarClick: (RegistroIluminacion) -> Unit,
            onEliminarClick: (RegistroIluminacion) -> Unit,
            onCompartirClick: (RegistroIluminacion) -> Unit
        ) {
            // Asigna la ubicación al texto principal
            txtUbicacion.text = registro.ubicacion
            // Muestra el nivel de luz ambiental formateado con su unidad de medida
            txtNivelLuzAmbiental.text = "Luz ambiental: ${registro.nivelLuzAmbiental} lx"
            // Muestra la intensidad de luz en formato numérico porcentual
            txtIntensidadLuz.text = "Intensidad: ${registro.intensidadLuz}%"
            // Asigna el progreso numérico al ProgressBar visual (0-100)
            pbIntensidadLuz.progress = registro.intensidadLuz
            // Muestra la fecha y hora registrada
            txtFechaRegistro.text = "Fecha: ${registro.fechaRegistro}"

            // Al hacer clic en cualquier parte de la fila, se dispara la función para editar
            itemView.setOnClickListener {
                onEditarClick(registro)
            }

            // Al hacer clic en el botón de compartir, se dispara la función de exportación
            btnCompartirRegistro.setOnClickListener {
                onCompartirClick(registro)
            }

            // Al hacer clic en el botón de la papelera, se dispara la función para eliminar
            btnEliminarRegistro.setOnClickListener {
                onEliminarClick(registro)
            }
        }
    }
}
