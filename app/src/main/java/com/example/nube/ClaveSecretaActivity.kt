package com.example.nube

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ClaveSecretaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clave_secreta)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etClaveSecreta: EditText = findViewById(R.id.etClaveSecreta)
        val btnVerificar: Button = findViewById(R.id.btnVerificarClave)
        val btnSolicitarNueva: Button = findViewById(R.id.btnSolicitarClaveNueva)

        // Botón para verificar clave secreta
        btnVerificar.setOnClickListener {
            val claveIngresada = etClaveSecreta.text.toString()
            val userId = auth.currentUser?.uid

            if (claveIngresada.isNotEmpty() && userId != null) {
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        val claveSecretaGuardada = document.getString("secretKey")
                        if (claveSecretaGuardada == claveIngresada) {
                            // Clave correcta, redirigir al menú
                            val intent = Intent(this, MenuActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Clave incorrecta", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al verificar clave", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Ingresa la clave secreta", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para solicitar una nueva clave secreta
        btnSolicitarNueva.setOnClickListener {
            val userId = auth.currentUser?.uid

            if (userId != null) {
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        val ultimaSolicitud = document.getLong("lastSecretKeyRequest")
                        val ahora = System.currentTimeMillis()
                        val unDiaMillis = 24 * 60 * 60 * 1000 // Milisegundos en un día

                        // Verificar si ha pasado un día desde la última solicitud
                        if (ultimaSolicitud == null || (ahora - ultimaSolicitud) >= unDiaMillis) {
                            val nuevaClave = UUID.randomUUID().toString() // Generar nueva clave

                            // Actualizar clave secreta y registrar la solicitud
                            db.collection("users").document(userId)
                                .update(
                                    mapOf(
                                        "secretKey" to nuevaClave,
                                        "lastSecretKeyRequest" to ahora
                                    )
                                )
                                .addOnSuccessListener {
                                    sendSecretKeyByEmail(document.getString("email") ?: "", nuevaClave)
                                    Toast.makeText(this, "Clave nueva enviada al correo", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error al generar clave nueva", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Solo puedes solicitar una nueva clave por día", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al solicitar clave nueva", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun sendSecretKeyByEmail(email: String, secretKey: String) {
        // Implementa el envío de correo (Firebase Cloud Functions o JavaMail)
        Toast.makeText(this, "Correo enviado a $email", Toast.LENGTH_SHORT).show()
    }
}
