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

class GestionEmpresaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var empresasAdapter: EmpresasAdapter
    private var maxEmpresas = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_empresa)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val recyclerView: RecyclerView = findViewById(R.id.recyclerEmpresas)
        val btnAddEmpresa: Button = findViewById(R.id.btnAddEmpresa)

        // Configurar RecyclerView
        empresasAdapter = EmpresasAdapter(
            onEditClick = { empresaId -> editarEmpresa(empresaId) },
            onDeleteClick = { empresaId -> borrarEmpresa(userId, empresaId) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = this.empresasAdapter

        // Obtener plan del usuario y definir límite
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    when (document.getString("plan")) {
                        "FREE" -> maxEmpresas = 1
                        "MEDIUM" -> maxEmpresas = 3
                        "PRO" -> maxEmpresas = 10
                        else -> maxEmpresas = 1
                    }
                }

                // Cargar la lista de empresas del usuario
                cargarEmpresas(userId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener el plan: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Añadir nueva empresa
        btnAddEmpresa.setOnClickListener {
            db.collection("users").document(userId).collection("empresas").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.size() >= maxEmpresas) {
                        Toast.makeText(this, "Has alcanzado el límite de empresas para tu plan", Toast.LENGTH_SHORT).show()
                    } else {
                        // Redirigir a CrearEmpresaActivity
                        startActivity(Intent(this, CrearEmpresaActivity::class.java))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al verificar empresas: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargarEmpresas(userId: String) {
        db.collection("users").document(userId).collection("empresas").get()
            .addOnSuccessListener { snapshot ->
                val empresas = snapshot.documents.map { document ->
                    Empresa(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        direccion = document.getString("direccion") ?: "",
                        nit = document.getString("NIT") ?: "",
                        datos = document.getString("datos") ?: "",
                        telefono = document.getString("telefono") ?: "",
                        rubro = document.getString("rubro") ?: ""
                    )
                }
                empresasAdapter.submitList(empresas)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar empresas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editarEmpresa(empresaId: String) {
        // Lógica para redirigir a una actividad para editar la empresa
        val intent = Intent(this, EditarEmpresaActivity::class.java)
        intent.putExtra("empresaId", empresaId)
        startActivity(intent)
    }

    private fun borrarEmpresa(userId: String, empresaId: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Empresa")
            .setMessage("¿Estás seguro de que deseas eliminar esta empresa?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("users").document(userId).collection("empresas").document(empresaId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Empresa eliminada exitosamente", Toast.LENGTH_SHORT).show()
                        cargarEmpresas(userId)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar la empresa: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
