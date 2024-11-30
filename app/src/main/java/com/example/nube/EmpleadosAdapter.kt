package com.example.nube

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmpleadosAdapter(
    private val empleadosList: List<Empleado>,
    private val onEditClick: (Empleado) -> Unit,
    private val onDeleteClick: (Empleado) -> Unit
) : RecyclerView.Adapter<EmpleadosAdapter.EmpleadoViewHolder>() {

    inner class EmpleadoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvCargo: TextView = view.findViewById(R.id.tvCargo)
        val btnEdit: Button = view.findViewById(R.id.btnEditEmpleado)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteEmpleado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpleadoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empleado, parent, false)
        return EmpleadoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpleadoViewHolder, position: Int) {
        val empleado = empleadosList[position]
        holder.tvNombre.text = "${empleado.nombre} ${empleado.apellido}"
        holder.tvCargo.text = empleado.cargo

        holder.btnEdit.setOnClickListener { onEditClick(empleado) }
        holder.btnDelete.setOnClickListener { onDeleteClick(empleado) }
    }

    override fun getItemCount(): Int = empleadosList.size
}
