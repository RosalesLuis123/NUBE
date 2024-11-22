package com.example.nube

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView

class VentasAdapter(
    private val context: Context,
    private val ventas: List<Venta>
) : BaseAdapter(), ListAdapter {

    override fun getCount(): Int = ventas.size

    override fun getItem(position: Int): Any = ventas[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_venta, parent, false)
        val venta = ventas[position]

        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val btnVerDetalle: Button = view.findViewById(R.id.btnVerDetalle)

        tvFecha.text = "Fecha: ${venta.fecha}"
        tvTotal.text = "Total: $${venta.total}"

        btnVerDetalle.setOnClickListener {
            val intent = Intent(context, DetalleVentaActivity::class.java)
            intent.putExtra("ventaId", venta.id)
            context.startActivity(intent)
        }

        return view
    }
}
