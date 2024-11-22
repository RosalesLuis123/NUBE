package com.example.nube

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InventariosAdapter(
    private val onEditClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<InventariosAdapter.ArticuloViewHolder>() {

    private val articulos = mutableListOf<Articulo>()

    fun submitList(list: List<Articulo>) {
        articulos.clear()
        articulos.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticuloViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_articulo, parent, false)
        return ArticuloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticuloViewHolder, position: Int) {
        val articulo = articulos[position]
        holder.bind(articulo)
    }

    override fun getItemCount(): Int = articulos.size

    inner class ArticuloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvCod: TextView = itemView.findViewById(R.id.tvCod)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(articulo: Articulo) {
            tvNombre.text = articulo.nombre
            tvCod.text = "COD: ${articulo.cod}"
            btnEdit.setOnClickListener { onEditClick(articulo.id) }
            btnDelete.setOnClickListener { onDeleteClick(articulo.id) }
        }
    }
}
