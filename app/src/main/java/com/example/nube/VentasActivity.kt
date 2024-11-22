package com.example.nube

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VentasActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ventas)

        val listView: ListView = findViewById(R.id.lvVentas)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        cargarVentas(userId, listView)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarVentas(userId: String, listView: ListView) {
        db.collection("users").document(userId).collection("ventas").get()
            .addOnSuccessListener { snapshot ->
                val ventas = snapshot.documents.map { document ->
                    Venta(
                        id = document.id,
                        articulos = document.get("articulos") as? List<Map<String, Any>> ?: emptyList(),
                        total = document.getDouble("total") ?: 0.0,
                        fecha = document.getString("fecha") ?: ""
                    )
                }

                val adapter = VentasAdapter(this, ventas)
                listView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar ventas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
