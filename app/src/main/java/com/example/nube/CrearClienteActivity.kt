package com.example.nube

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearClienteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_cliente)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val etNombre: EditText = findViewById(R.id.etNombre)
        val etCI: EditText = findViewById(R.id.etCI)
        val btnGuardar: Button = findViewById(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val ci = etCI.text.toString()

            if (nombre.isEmpty() || ci.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cliente = hashMapOf(
                "nombre" to nombre,
                "ci" to ci
            )

            db.collection("users").document(userId).collection("clientes").add(cliente)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cliente creado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el cliente: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
