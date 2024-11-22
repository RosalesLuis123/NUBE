package com.example.nube

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmpresasAdapter(
    private val onEditClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<EmpresasAdapter.EmpresaViewHolder>() {

    private val empresas = mutableListOf<Empresa>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Empresa>) {
        empresas.clear()
        empresas.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empresa, parent, false)
        return EmpresaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        val empresa = empresas[position]
        holder.bind(empresa)
    }

    override fun getItemCount(): Int = empresas.size

    inner class EmpresaViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(empresa: Empresa) {
            tvNombre.text = empresa.nombre
            btnEdit.setOnClickListener { onEditClick(empresa.id) }
            btnDelete.setOnClickListener { onDeleteClick(empresa.id) }
        }
    }
}
