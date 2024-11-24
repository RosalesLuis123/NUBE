package com.example.nube

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Cliente(val ci: String, val nombre: String)

class GestionClientesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var clientesAdapter: ClientesAdapter
    private val clientesList = mutableListOf<Cliente>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_clientes)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerViewClientes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configurar el adaptador
        clientesAdapter = ClientesAdapter(clientesList, ::editCliente, ::deleteCliente)
        recyclerView.adapter = clientesAdapter

        val btnAddCliente: Button = findViewById(R.id.btnAddCliente)
        btnAddCliente.setOnClickListener { addCliente() }

        // Cargar clientes desde Firestore
        loadClientes()
    }

    private fun loadClientes() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        db.collection("users")
            .document(userId)
            .collection("clientes")
            .get()
            .addOnSuccessListener { querySnapshot ->
                clientesList.clear()
                for (document in querySnapshot) {
                    val cliente = Cliente(
                        ci = document.getString("ci") ?: "",
                        nombre = document.getString("nombre") ?: ""
                    )
                    clientesList.add(cliente)
                }
                clientesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar clientes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addCliente() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_cliente, null)
        val ciInput = view.findViewById<EditText>(R.id.inputCi)
        val nombreInput = view.findViewById<EditText>(R.id.inputNombre)

        AlertDialog.Builder(this)
            .setTitle("Añadir Cliente")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val ci = ciInput.text.toString()
                val nombre = nombreInput.text.toString()

                if (ci.isNotBlank() && nombre.isNotBlank()) {
                    val cliente = hashMapOf("ci" to ci, "nombre" to nombre)
                    db.collection("users")
                        .document(userId)
                        .collection("clientes")
                        .add(cliente)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cliente añadido exitosamente", Toast.LENGTH_SHORT).show()
                            loadClientes()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al añadir cliente: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun editCliente(cliente: Cliente) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_cliente, null)
        val ciInput = view.findViewById<EditText>(R.id.inputCi)
        val nombreInput = view.findViewById<EditText>(R.id.inputNombre)

        ciInput.setText(cliente.ci)
        nombreInput.setText(cliente.nombre)

        AlertDialog.Builder(this)
            .setTitle("Editar Cliente")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val ci = ciInput.text.toString()
                val nombre = nombreInput.text.toString()

                if (ci.isNotBlank() && nombre.isNotBlank()) {
                    // Actualizar cliente
                    db.collection("users")
                        .document(userId)
                        .collection("clientes")
                        .whereEqualTo("ci", cliente.ci)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot) {
                                db.collection("users")
                                    .document(userId)
                                    .collection("clientes")
                                    .document(document.id)
                                    .update("ci", ci, "nombre", nombre)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Cliente actualizado exitosamente", Toast.LENGTH_SHORT).show()
                                        loadClientes()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error al actualizar cliente: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                } else {
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun deleteCliente(cliente: Cliente) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        AlertDialog.Builder(this)
            .setTitle("Eliminar Cliente")
            .setMessage("¿Estás seguro de que deseas eliminar a ${cliente.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("users")
                    .document(userId)
                    .collection("clientes")
                    .whereEqualTo("ci", cliente.ci)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot) {
                            db.collection("users")
                                .document(userId)
                                .collection("clientes")
                                .document(document.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                                    loadClientes()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al eliminar cliente: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }
}
