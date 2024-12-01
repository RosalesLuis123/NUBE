package com.example.nube

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase Authentication y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnRegister: Button = findViewById(R.id.btnRegister)
        val etEmail: EditText = findViewById(R.id.etEmail)
        val etPassword: EditText = findViewById(R.id.etPassword)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val secretKey = generateSecretKey()

                        // Crear un mapa con los datos del usuario
                        val userMap = mapOf(
                            "email" to email,
                            "plan" to "FREE", // Plan inicial
                            "secretKey" to secretKey, // Clave secreta
                            "lastSecretKeyRequest" to null // Campo para gestionar solicitudes de nueva clave
                        )

                        // Guardar datos del usuario en Firestore
                        db.collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                // Enviar clave secreta al correo electrónico
                                sendSecretKeyByEmail(email, secretKey)

                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun generateSecretKey(): String {
        // Generar una clave secreta única
        return UUID.randomUUID().toString()
    }

    private fun sendSecretKeyByEmail(email: String, secretKey: String) {
        // Aquí puedes implementar el envío real del correo
        // Por ejemplo, utilizando Firebase Cloud Functions o JavaMail
        Toast.makeText(this, "Clave secreta enviada a $email", Toast.LENGTH_SHORT).show()
    }
}
