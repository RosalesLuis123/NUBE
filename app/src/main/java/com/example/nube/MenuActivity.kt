package com.example.nube

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val btnPersonas: Button = findViewById(R.id.btnPersonas)
        val btnEmpresas: Button = findViewById(R.id.btnEmpresas)
        val btnGraficas: Button = findViewById(R.id.btnGraficas)
        val btnIA: Button = findViewById(R.id.btnIA)
        val btnAnalisis: Button = findViewById(R.id.btnAnalisis)

        // Obtener el plan del usuario desde Firestore
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val plan = document.getString("plan")
                    when (plan) {
                        "FREE" -> {
                            btnGraficas.isEnabled = false
                            btnIA.isEnabled = false
                            btnAnalisis.isEnabled = false
                        }
                        "MEDIUM" -> {
                            btnIA.isEnabled = false
                            btnAnalisis.isEnabled = false
                        }
                        // PRO tiene acceso completo
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener el plan: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        btnPersonas.setOnClickListener { /* Ir a PersonasActivity */ }
        btnEmpresas.setOnClickListener {
            val intent = Intent(this, EmpresaMenuActivity::class.java)
            startActivity(intent)
        }
        btnGraficas.setOnClickListener {
            val intent = Intent(this, GraficosActivity::class.java)
            startActivity(intent)
        }
        btnIA.setOnClickListener { /* Ir a IAActivity */ }
        btnAnalisis.setOnClickListener { /* Ir a AnalisisActivity */ }
    }
}
