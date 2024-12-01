package com.example.nube

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class FacturacionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val articulosSeleccionados = mutableListOf<ArticuloSeleccionado>()

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facturacion)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val spEmpresa: Spinner = findViewById(R.id.spEmpresa)
        val spCliente: Spinner = findViewById(R.id.spCliente)
        val btnAddCliente: Button = findViewById(R.id.btnAddCliente)
        val btnAddArticulo: Button = findViewById(R.id.btnAddArticulo)
        val lvArticulos: ListView = findViewById(R.id.lvArticulos)
        val btnGuardarFactura: Button = findViewById(R.id.btnGuardarFactura)

        // Cargar empresas
        cargarEmpresas(userId, spEmpresa)

        // Cargar clientes
        cargarClientes(userId, spCliente)

        // Añadir cliente
        btnAddCliente.setOnClickListener {
            startActivity(Intent(this, CrearClienteActivity::class.java))
        }

        // Añadir artículo a la lista de artículos seleccionados
        btnAddArticulo.setOnClickListener {
            seleccionarArticulo(userId, lvArticulos)
        }

        // Guardar venta
        btnGuardarFactura.setOnClickListener {
            val empresaId = spEmpresa.selectedItem as? String
            val clienteId = spCliente.selectedItem as? String

            if (empresaId == null || clienteId == null) {
                Toast.makeText(this, "Selecciona una empresa y un cliente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (articulosSeleccionados.isEmpty()) {
                Toast.makeText(this, "Añade al menos un artículo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarVenta(userId, empresaId, clienteId)
        }
    }

    private fun cargarEmpresas(userId: String, spinner: Spinner) {
        db.collection("users").document(userId).collection("empresas").get()
            .addOnSuccessListener { snapshot ->
                val empresas = snapshot.documents.mapNotNull { it.getString("nombre") }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, empresas)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar empresas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarClientes(userId: String, spinner: Spinner) {
        db.collection("users").document(userId).collection("clientes").get()
            .addOnSuccessListener { snapshot ->
                val clientes = snapshot.documents.mapNotNull { it.getString("nombre") }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clientes)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar clientes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun seleccionarArticulo(userId: String, listView: ListView) {
        db.collection("users").document(userId).collection("articulos").get()
            .addOnSuccessListener { snapshot ->
                val articulos = snapshot.documents.map { document ->
                    Articulo(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        cantidad = document.getLong("cantidad")?.toInt() ?: 0,
                        precioUnitario = document.getDouble("precioUnitario") ?: 0.0
                    )
                }

                val nombresArticulos = articulos.map { it.nombre }
                AlertDialog.Builder(this)
                    .setTitle("Seleccionar Artículo")
                    .setItems(nombresArticulos.toTypedArray()) { _, index ->
                        val articuloSeleccionado = articulos[index]
                        val cantidad = solicitarCantidad()
                        if (cantidad > 0) {
                            articulosSeleccionados.add(
                                ArticuloSeleccionado(
                                    id = articuloSeleccionado.id,
                                    nombre = articuloSeleccionado.nombre,
                                    cantidad = cantidad,
                                    precioUnitario = articuloSeleccionado.precioUnitario
                                )
                            )
                            actualizarListaArticulos(listView)
                        }
                    }
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar artículos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun solicitarCantidad(): Int {
        // Aquí debes implementar un cuadro de diálogo para solicitar la cantidad.
        return 1 // Esto es un ejemplo; implementa un diálogo real.
    }

    private fun actualizarListaArticulos(listView: ListView) {
        val nombresArticulos = articulosSeleccionados.map { "${it.nombre} (x${it.cantidad})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombresArticulos)
        listView.adapter = adapter
    }

    private fun guardarVenta(userId: String, empresaId: String, clienteId: String) {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val total = articulosSeleccionados.sumOf { it.cantidad * it.precioUnitario }

        // Obtener el CI y nombre del empleado logueado
        val empleadoRef = db.collection("users").document(userId).collection("empleados")
        val ciEmpleado = "12345678" // Aquí deberías obtener el CI que corresponde al empleado logueado

        empleadoRef.whereEqualTo("ci", ciEmpleado) // Buscar por el CI del empleado
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val empleado = snapshot.documents.first()
                    val ciEmpleado = empleado.getString("ci") ?: ""
                    val nombreEmpleado = empleado.getString("nombre") ?: ""

                    val venta = hashMapOf(
                        "empresaId" to empresaId,
                        "clienteId" to clienteId,
                        "empleadoCi" to ciEmpleado,
                        "empleadoNombre" to nombreEmpleado,
                        "articulos" to articulosSeleccionados.map {
                            hashMapOf(
                                "id" to it.id,
                                "nombre" to it.nombre,
                                "cantidad" to it.cantidad,
                                "precioUnitario" to it.precioUnitario
                            )
                        },
                        "fecha" to fecha,
                        "total" to total
                    )

                    // Guardar en la colección de ventas
                    db.collection("users").document(userId).collection("ventas").add(venta)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Venta creada exitosamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar la venta: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Empleado no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener datos del empleado: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
