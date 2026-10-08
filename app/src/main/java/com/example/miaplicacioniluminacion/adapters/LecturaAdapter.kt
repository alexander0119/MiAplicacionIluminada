package com.example.miaplicacioniluminacion.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miaplicacioniluminacion.R
import com.example.miaplicacioniluminacion.models.Lectura

// El constructor ahora recibe dos funciones (lambdas): una para editar y otra para eliminar
class LecturaAdapter(
    private var listaLecturas: List<Lectura> = listOf(),
    private val onEditarClick: (Lectura) -> Unit,
    private val onEliminarClick: (Lectura) -> Unit
) : RecyclerView.Adapter<LecturaAdapter.LecturaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LecturaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lectura, parent, false)
        return LecturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LecturaViewHolder, position: Int) {
        val lectura = listaLecturas[position]
        // Le pasamos al bind la lectura y ambas funciones de clic
        holder.bind(lectura, onEditarClick, onEliminarClick)
    }

    override fun getItemCount(): Int {
        return listaLecturas.size
    }

    fun actualizarLista(nuevaLista: List<Lectura>) {
        listaLecturas = nuevaLista
        notifyDataSetChanged()
    }

    class LecturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtDescripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
        private val txtValor: TextView = itemView.findViewById(R.id.txtValor)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)

        fun bind(lectura: Lectura, onEditarClick: (Lectura) -> Unit, onEliminarClick: (Lectura) -> Unit) {
            txtDescripcion.text = lectura.descripcion
            txtValor.text = "Valor: ${lectura.valor}"

            // Configuramos qué pasa cuando alguien toca CUALQUIER PARTE de la fila (para editar)
            itemView.setOnClickListener {
                onEditarClick(lectura)
            }

            // Configuramos qué pasa cuando alguien toca ESPECÍFICAMENTE el basurero (para eliminar)
            btnEliminar.setOnClickListener {
                onEliminarClick(lectura)
            }
        }
    }
}
