package com.example.nube

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class GraficosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graficos)

        val barChart: BarChart = findViewById(R.id.barChart)
        val lineChart: LineChart = findViewById(R.id.lineChart)
        val pieChart: PieChart = findViewById(R.id.pieChart)
        val barChartByArticle: BarChart = findViewById(R.id.barChartByArticle)
        val stackedBarChart: BarChart = findViewById(R.id.stackedBarChart)
        val scatterChart: ScatterChart = findViewById(R.id.scatterChart)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return

        cargarDatosVentas(userId, barChart, lineChart, pieChart, barChartByArticle, stackedBarChart, scatterChart)
    }

    private fun cargarDatosVentas(userId: String, barChart: BarChart, lineChart: LineChart, pieChart: PieChart,
                                  barChartByArticle: BarChart, stackedBarChart: BarChart, scatterChart: ScatterChart) {
        db.collection("users").document(userId).collection("ventas").get()
            .addOnSuccessListener { snapshot ->
                val ventas = snapshot.documents.mapNotNull { document ->
                    Venta(
                        id = document.id,
                        articulos = document.get("articulos") as? List<Map<String, Any>> ?: emptyList(),
                        total = document.getDouble("total") ?: 0.0,
                        fecha = document.getString("fecha") ?: ""
                    )
                }

                generarGraficoBarras(barChart, ventas)
                generarGraficoLineas(lineChart, ventas)
                generarGraficoTorta(pieChart, ventas)
                generarGraficoBarrasPorArticulo(barChartByArticle, ventas)
                generarGraficoBarrasApiladas(stackedBarChart, ventas)
                generarGraficoDispersión(scatterChart, ventas)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }


    private fun generarGraficoBarras(barChart: BarChart, ventas: List<Venta>) {
        val entradas = ventas.mapIndexed { index, venta ->
            BarEntry(index.toFloat(), venta.total.toFloat()) // Ensuring that total is converted to Float
        }

        val dataSet = BarDataSet(entradas, "Ventas por Fecha")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.description.text = "Totales por Fecha"
        barChart.invalidate()
    }


    private fun generarGraficoLineas(lineChart: LineChart, ventas: List<Venta>) {
        val entradas = ventas.mapIndexed { index, venta ->
            BarEntry(index.toFloat(), venta.total.toFloat())
        }

        val dataSet = LineDataSet(entradas, "Evolución de Ventas")
        dataSet.color = ColorTemplate.COLORFUL_COLORS[0]

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.description.text = "Evolución de Ventas"
        lineChart.invalidate()
    }

    private fun generarGraficoTorta(pieChart: PieChart, ventas: List<Venta>) {
        val articulosTotales = mutableMapOf<String, Float>()

        ventas.forEach { venta ->
            venta.articulos.forEach { articulo ->
                val nombre = articulo["nombre"] as? String ?: "Sin nombre"
                val cantidad = (articulo["cantidad"] as? Number)?.toFloat() ?: 0f
                articulosTotales[nombre] = articulosTotales.getOrDefault(nombre, 0f) + cantidad
            }
        }

        val entradas = articulosTotales.map { (nombre, cantidad) ->
            PieEntry(cantidad, nombre)
        }

        val dataSet = PieDataSet(entradas, "Distribución de Artículos")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val pieData = PieData(dataSet)
        pieChart.data = pieData

        pieChart.description.text = "Artículos Vendidos"
        pieChart.invalidate()
    }
    private fun generarGraficoBarrasPorArticulo(barChartByArticle: BarChart, ventas: List<Venta>) {
        // Crear un mapa para contar la cantidad total de cada artículo
        val articulosTotales = mutableMapOf<String, Float>()

        // Iterar sobre las ventas para acumular las cantidades de cada artículo
        ventas.forEach { venta ->
            venta.articulos.forEach { articulo ->
                val nombre = articulo["nombre"] as? String ?: "Sin nombre"
                val cantidad = (articulo["cantidad"] as? Number)?.toFloat() ?: 0f

                // Sumar la cantidad de cada artículo
                articulosTotales[nombre] = articulosTotales.getOrDefault(nombre, 0f) + cantidad
            }
        }

        // Crear las entradas para el gráfico de barras
        val entradas = articulosTotales.toList().mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.second) // Usamos 'index' para cada barra y 'entry.second' es la cantidad
        }

        // Crear el conjunto de datos para el gráfico de barras
        val dataSet = BarDataSet(entradas, "Ventas por Artículo")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        // Crear los datos del gráfico
        val barData = BarData(dataSet)

        // Asignar los datos al gráfico
        barChartByArticle.data = barData

        // Establecer las etiquetas para el eje X con los nombres de los artículos
        val nombresArticulos = articulosTotales.keys.toList()
        val xAxis = barChartByArticle.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(nombresArticulos)

        // Configurar el gráfico
        barChartByArticle.description.text = "Ventas por Artículo"
        barChartByArticle.invalidate()
    }

    private fun generarGraficoBarrasApiladas(stackedBarChart: BarChart, ventas: List<Venta>) {
        val entradas = mutableListOf<BarEntry>()
        val articulosTotalesPorFecha = mutableMapOf<String, MutableList<Float>>()

        ventas.forEach { venta ->
            val fecha = venta.fecha
            venta.articulos.forEach { articulo ->
                val nombre = articulo["nombre"] as? String ?: "Sin nombre"
                val cantidad = (articulo["cantidad"] as? Number)?.toFloat() ?: 0f

                articulosTotalesPorFecha.computeIfAbsent(fecha) { mutableListOf() }.add(cantidad)
            }
        }

        // Convertir las fechas a milisegundos
        articulosTotalesPorFecha.forEach { (fecha, cantidades) ->
            val fechaEnMilisegundos = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)?.time?.toFloat() ?: 0f
            val entry = BarEntry(fechaEnMilisegundos, cantidades.toFloatArray())
            entradas.add(entry)
        }

        val dataSet = BarDataSet(entradas, "Ventas Apiladas por Fecha")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val barData = BarData(dataSet)
        stackedBarChart.data = barData

        stackedBarChart.description.text = "Ventas Apiladas por Artículo"
        stackedBarChart.invalidate()
    }


    private fun generarGraficoDispersión(scatterChart: ScatterChart, ventas: List<Venta>) {
        val entradas = ventas.mapIndexed { index, venta ->
            // Use 'Entry' instead of 'ScatterEntry'
            com.github.mikephil.charting.data.Entry(venta.articulos.size.toFloat(), venta.total.toFloat())
        }

        val dataSet = ScatterDataSet(entradas, "Relación Artículos y Ventas")
        dataSet.color = ColorTemplate.COLORFUL_COLORS[0]

        val scatterData = ScatterData(dataSet)
        scatterChart.data = scatterData

        scatterChart.description.text = "Relación Artículos y Ventas"
        scatterChart.invalidate()
    }
}
