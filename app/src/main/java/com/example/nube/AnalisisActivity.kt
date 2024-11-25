package com.example.nube

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import android.view.View
import java.util.*

class AnalisisActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var spnPeriodo: Spinner
    private lateinit var btnFecha: Button
    private lateinit var btnAnalizar: Button
    private lateinit var tvResultado: TextView

    private var fechaSeleccionada: Calendar = Calendar.getInstance()
    private var periodoSeleccionado: String = "Día"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analisis)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Referenciar vistas
        spnPeriodo = findViewById(R.id.spnPeriodo)
        btnFecha = findViewById(R.id.btnFecha)
        btnAnalizar = findViewById(R.id.btnAnalizar)
        tvResultado = findViewById(R.id.tvResultado)

        // Configurar el Spinner con opciones de análisis
        val opciones = listOf("Día", "Mes", "Año")
        spnPeriodo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)

        spnPeriodo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                periodoSeleccionado = opciones[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Selección de fecha
        btnFecha.setOnClickListener { mostrarDatePicker() }

        // Botón para realizar el análisis
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
                "Día" -> {
                    // Mantener solo la fecha seleccionada
                }
                "Mes" -> set(Calendar.DAY_OF_MONTH, 1) // Primer día del mes
                "Año" -> set(Calendar.DAY_OF_YEAR, 1) // Primer día del año
            }
        }

        val fechaFin = Calendar.getInstance().apply {
            time = fechaSeleccionada.time
            when (periodoSeleccionado) {
                "Día" -> add(Calendar.DAY_OF_MONTH, 1) // Día siguiente
                "Mes" -> {
                    set(Calendar.DAY_OF_MONTH, 1) // Primer día del mes siguiente
                    add(Calendar.MONTH, 1)
                }
                "Año" -> {
                    set(Calendar.DAY_OF_YEAR, 1) // Primer día del año siguiente
                    add(Calendar.YEAR, 1)
                }
            }
        }

        val fechaInicioStr = dateFormat.format(fechaInicio.time)
        val fechaFinStr = dateFormat.format(fechaFin.time)

        // Consultar en Firestore
        db.collection("users").document(userId).collection("ventas")
            .whereGreaterThanOrEqualTo("fecha", fechaInicioStr)
            .whereLessThan("fecha", fechaFinStr)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    tvResultado.text = "No se encontraron ventas para este rango."
                    return@addOnSuccessListener
                }

                // Agrupar ventas por fecha
                val ventasPorFecha = mutableMapOf<String, Double>()
                for (document in snapshot.documents) {
                    val fecha = document.getString("fecha") ?: continue
                    val total = document.getDouble("total") ?: 0.0
                    ventasPorFecha[fecha] = ventasPorFecha.getOrDefault(fecha, 0.0) + total
                }

                // Encontrar la fecha con más y menos ventas
                val fechaMasVentas = ventasPorFecha.maxByOrNull { it.value }
                val fechaMenosVentas = ventasPorFecha.minByOrNull { it.value }

                val totalVentas = ventasPorFecha.values.sum()
                val cantidadVentas = ventasPorFecha.size

                // Mostrar resultados
                tvResultado.text = """
                    Análisis ($periodoSeleccionado):
                    Ventas realizadas: $cantidadVentas
                    Total recaudado: $totalVentas
                    
                    Fecha con más ventas:
                    ${fechaMasVentas?.key} (${fechaMasVentas?.value ?: 0.0})
                    
                    Fecha con menos ventas:
                    ${fechaMenosVentas?.key} (${fechaMenosVentas?.value ?: 0.0})
                """.trimIndent()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al realizar análisis: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}