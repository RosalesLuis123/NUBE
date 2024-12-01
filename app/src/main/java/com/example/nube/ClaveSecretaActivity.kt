package com.example.nube

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.util.UUID

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
                showPasswordDialog(userId)
            } else {
                Toast.makeText(this, "No estás autenticado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPasswordDialog(userId: String) {
        val input = EditText(this).apply {
            hint = "Ingresa tu contraseña"
            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Verificar contraseña")
            .setMessage("Por favor ingresa tu contraseña para continuar")
            .setView(input)
            .setPositiveButton("Aceptar") { _, _ ->
                val password = input.text.toString()
                if (password.isNotEmpty()) {
                    verifyPasswordAndGenerateKey(userId, password)
                } else {
                    Toast.makeText(this, "Por favor ingresa una contraseña", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun verifyPasswordAndGenerateKey(userId: String, password: String) {
        // Aquí debes verificar que la contraseña ingresada coincida con la almacenada en Firebase
        val user = auth.currentUser
        if (user != null) {
            user.reauthenticate(EmailAuthProvider.getCredential(user.email!!, password))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Contraseña verificada, generar nueva clave secreta
                        val nuevaClave = UUID.randomUUID().toString() // Generar nueva clave
                        updateSecretKey(userId, nuevaClave)
                    } else {
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun updateSecretKey(userId: String, nuevaClave: String) {
        // Actualizar clave secreta en Firestore
        db.collection("users").document(userId)
            .update("secretKey", nuevaClave)
            .addOnSuccessListener {
                showSecretKeyDialog(nuevaClave)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al generar clave nueva", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSecretKeyDialog(secretKey: String) {
        // Crear el diálogo que muestra la clave secreta y permite copiarla
        val dialog = AlertDialog.Builder(this)
            .setTitle("Tu nueva clave secreta")
            .setMessage(secretKey)
            .setPositiveButton("Copiar") { _, _ ->
                // Copiar la clave al portapapeles
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Clave secreta", secretKey)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Clave copiada al portapapeles", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cerrar", null)
            .create()

        dialog.show()
    }
}

