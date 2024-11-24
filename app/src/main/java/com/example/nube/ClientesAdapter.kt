package com.example.nube

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KFunction1


class ClientesAdapter(
    private val clientes: MutableList<Cliente>,
    private val onEdit: KFunction1<Cliente, Unit>,
    private val onDelete: KFunction1<Cliente, Unit>
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ciText: TextView = view.findViewById(R.id.textCi)
        val nombreText: TextView = view.findViewById(R.id.textNombre)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.ciText.text = cliente.ci
        holder.nombreText.text = cliente.nombre

        holder.btnEdit.setOnClickListener { onEdit(cliente) }
        holder.btnDelete.setOnClickListener { onDelete(cliente) }
    }

    override fun getItemCount(): Int = clientes.size
}
