package com.example.nube

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlanActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan)

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtener el usuario actual logueado
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
            finish() // Cerrar la actividad si no hay usuario logueado
            return
        }

        val userEmail = currentUser.email ?: ""

        // Botones para seleccionar el plan
        val freeButton: Button = findViewById(R.id.btn_free)
        val mediumButton: Button = findViewById(R.id.btn_medium)
        val proButton: Button = findViewById(R.id.btn_pro)

        // Asignar eventos de clic
        freeButton.setOnClickListener { updatePlan(userEmail, "FREE") }
        mediumButton.setOnClickListener { updatePlan(userEmail, "MEDIUM") }
        proButton.setOnClickListener { updatePlan(userEmail, "PRO") }
    }

    private fun updatePlan(email: String, newPlan: String) {
        // Buscar el documento del usuario en Firestore y actualizar su plan
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val docId = document.id

                    db.collection("users").document(docId)
                        .update("plan", newPlan)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Plan cambiado a $newPlan", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al cambiar el plan: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al buscar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}