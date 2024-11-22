package com.example.nube

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InventariosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var inventariosAdapter: InventariosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventarios)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val recyclerView: RecyclerView = findViewById(R.id.recyclerInventarios)
        val btnAddArticulo: Button = findViewById(R.id.btnAddArticulo)

        // Configurar RecyclerView
        inventariosAdapter = InventariosAdapter(
            onEditClick = { articuloId -> editarArticulo(articuloId) },
            onDeleteClick = { articuloId -> borrarArticulo(userId, articuloId) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = inventariosAdapter

        // Cargar lista de artículos
        cargarArticulos(userId)

        // Botón Añadir Artículo
        btnAddArticulo.setOnClickListener {
            startActivity(Intent(this, CrearArticuloActivity::class.java))
        }
    }

    private fun cargarArticulos(userId: String) {
        db.collection("users").document(userId).collection("articulos").get()
            .addOnSuccessListener { snapshot ->
                val articulos = snapshot.documents.map { document ->
                    Articulo(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        cod = document.getString("cod") ?: "",
                        cantidad = document.getLong("cantidad")?.toInt() ?: 0,
                        precioUnitario = document.getDouble("precioUnitario") ?: 0.0,
                        tipo = document.getString("tipo") ?: "",
                        detalle = document.getString("detalle") ?: ""
                    )
                }
                inventariosAdapter.submitList(articulos)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar los artículos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editarArticulo(articuloId: String) {
        val intent = Intent(this, EditarArticuloActivity::class.java)
        intent.putExtra("articuloId", articuloId)
        startActivity(intent)
    }

    private fun borrarArticulo(userId: String, articuloId: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Artículo")
            .setMessage("¿Estás seguro de que deseas eliminar este artículo?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("users").document(userId).collection("articulos").document(articuloId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Artículo eliminado exitosamente", Toast.LENGTH_SHORT).show()
                        cargarArticulos(userId)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar el artículo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
