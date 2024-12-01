package com.example.nube

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetalleVentaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_venta)

        val listView: ListView = findViewById(R.id.lvArticulosVenta)
        val tvFecha: TextView = findViewById(R.id.tvFechaVenta)
        val tvTotal: TextView = findViewById(R.id.tvTotalVenta)
        val tvNombreEmpleado: TextView = findViewById(R.id.tvNombreEmpleado)
        val tvCIEmpleado: TextView = findViewById(R.id.tvCIEmpleado)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val ventaId = intent.getStringExtra("ventaId") ?: return

        db.collection("users").document(userId).collection("ventas").document(ventaId).get()
            .addOnSuccessListener { document ->
                val articulos = document.get("articulos") as? List<Map<String, Any>> ?: emptyList()
                val fecha = document.getString("fecha") ?: ""
                val total = document.getDouble("total") ?: 0.0
                val empleadoNombre = document.getString("empleadoNombre") ?: "Empleado no encontrado"
                val empleadoCi = document.getString("empleadoCi") ?: "CI no encontrado"

                // Actualiza la UI con los datos de la venta
                tvFecha.text = "Fecha: $fecha"
                tvTotal.text = "Total: $total"
                tvNombreEmpleado.text = "Nombre del empleado: $empleadoNombre"
                tvCIEmpleado.text = "CI del empleado: $empleadoCi"

                // Prepara la lista de artículos
                val articulosTexto = articulos.map {
                    "${it["nombre"]} (x${it["cantidad"]}) - $${it["precioUnitario"]}"
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, articulosTexto)
                listView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar detalles de la venta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
