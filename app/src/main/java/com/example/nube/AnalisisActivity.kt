package com.example.nube

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AnalisisActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var spnPeriodo: Spinner
    private lateinit var spnFiltro: Spinner
    private lateinit var btnFecha: Button
    private lateinit var btnAnalizar: Button
    private lateinit var tvResultado: TextView
    private lateinit var listView: ListView

    private var fechaSeleccionada: Calendar = Calendar.getInstance()
    private var periodoSeleccionado: String = "Día"
    private var filtroSeleccionado: String = "General"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analisis)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Referenciar vistas
        spnPeriodo = findViewById(R.id.spnPeriodo)
        spnFiltro = findViewById(R.id.spnFiltro)
        btnFecha = findViewById(R.id.btnFecha)
        btnAnalizar = findViewById(R.id.btnAnalizar)
        tvResultado = findViewById(R.id.tvResultado)
        listView = findViewById(R.id.listViewResultados)

        // Configurar Spinner para periodo
        val opcionesPeriodo = listOf("Día", "Mes", "Año")
        spnPeriodo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesPeriodo)
        spnPeriodo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                periodoSeleccionado = opcionesPeriodo[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Configurar Spinner para filtro
        val opcionesFiltro = listOf("General", "Empleado", "Producto")
        spnFiltro.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesFiltro)
        spnFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filtroSeleccionado = opcionesFiltro[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Configurar selección de fecha
        btnFecha.setOnClickListener { mostrarDatePicker() }

        // Configurar análisis
        btnAnalizar.setOnClickListener { realizarAnalisis() }
    }

    private fun mostrarDatePicker() {
        val year = fechaSeleccionada.get(Calendar.YEAR)
        val month = fechaSeleccionada.get(Calendar.MONTH)
        val day = fechaSeleccionada.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            fechaSeleccionada.set(selectedYear, selectedMonth, selectedDay)
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            btnFecha.text = formatoFecha.format(fechaSeleccionada.time)
        }, year, month, day).show()
    }

    private fun realizarAnalisis() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        // Formato de fecha en Firestore
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Determinar rango de fechas
        val fechaInicio = Calendar.getInstance().apply {
            time = fechaSeleccionada.time
            when (periodoSeleccionado) {
                "Día" -> {}
                "Mes" -> set(Calendar.DAY_OF_MONTH, 1)
                "Año" -> set(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val fechaFin = Calendar.getInstance().apply {
            time = fechaSeleccionada.time
            when (periodoSeleccionado) {
                "Día" -> add(Calendar.DAY_OF_MONTH, 1)
                "Mes" -> {
                    set(Calendar.DAY_OF_MONTH, 1)
                    add(Calendar.MONTH, 1)
                }
                "Año" -> {
                    set(Calendar.DAY_OF_YEAR, 1)
                    add(Calendar.YEAR, 1)
                }
            }
        }

        val fechaInicioStr = dateFormat.format(fechaInicio.time)
        val fechaFinStr = dateFormat.format(fechaFin.time)

        // Consultar en Firestore según filtro (General, Empleado, Producto)
        db.collection("users").document(userId).collection("ventas")
            .whereGreaterThanOrEqualTo("fecha", fechaInicioStr)
            .whereLessThan("fecha", fechaFinStr)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    tvResultado.text = "No se encontraron ventas para este rango."
                    listView.adapter = null
                    return@addOnSuccessListener
                }

                val resultados = mutableListOf<String>()
                for (document in snapshot.documents) {
                    val fecha = document.getString("fecha") ?: "Sin fecha"
                    val total = document.getDouble("total") ?: 0.0
                    val articulos = document.get("articulos") as? List<Map<String, Any>> ?: emptyList()
                    val clienteId = document.getString("clienteId") ?: "Cliente desconocido"
                    val empresaId = document.getString("empresaId") ?: "Empresa desconocida"
                    val empleadoCi = document.getString("empleadoCi") ?: "Empleado desconocido"
                    val empleadoNombre = document.getString("empleadoNombre") ?: "Empleado desconocido"

                    // Realizar análisis dependiendo del filtro seleccionado
                    when (filtroSeleccionado) {
                        "General" -> {
                            resultados.add("Factura Fecha: $fecha | Total: $total | Cliente: $clienteId | Empresa: $empresaId | Empleado: $empleadoNombre")
                            // Agregar los artículos de la factura
                            for (articulo in articulos) {
                                val nombre = articulo["nombre"] ?: "Artículo desconocido"
                                val cantidad = articulo["cantidad"] ?: 0
                                val precio = articulo["precio"] ?: 0.0
                                resultados.add(" - Artículo: $nombre (x$cantidad) | Precio: $precio")
                            }
                        }
                        "Empleado" -> {
                            if (empleadoCi != "Empleado desconocido") {
                                resultados.add("Empleado: $empleadoNombre | Factura Fecha: $fecha | Total: $total")
                            }
                        }
                        "Producto" -> {
                            for (articulo in articulos) {
                                val productoNombre = articulo["nombre"] as? String ?: "Producto desconocido"
                                val cantidad = articulo["cantidad"] ?: 0
                                val precio = articulo["precio"] ?: 0.0
                                resultados.add("Factura Fecha: $fecha | Total: $total | Producto: $productoNombre (x$cantidad) | Precio: $precio")
                            }
                        }
                    }
                }

                // Mostrar los resultados
                tvResultado.text = "Análisis Completo"
                val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resultados)
                listView.adapter = arrayAdapter
            }
            .addOnFailureListener { e ->
                tvResultado.text = "Error al obtener los datos: ${e.message}"
            }
    }

}
