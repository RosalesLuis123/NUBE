package com.example.nube

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginEmpleadoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_empleado)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val inputCi = findViewById<EditText>(R.id.inputCi)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Manejo del login del empleado
        btnLogin.setOnClickListener {
            val ci = inputCi.text.toString()
            val password = inputPassword.text.toString()

            if (ci.isNotEmpty() && password.isNotEmpty()) {
                loginEmpleado(ci, password)
            } else {
                Toast.makeText(this, "Por favor ingrese su CI y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para autenticar al empleado
    private fun loginEmpleado(ci: String, password: String) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("empleados")
                .whereEqualTo("ci", ci)
                .whereEqualTo("password", password)  // Verificar contraseña directamente sin encriptación
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Toast.makeText(this, "Empleado no encontrado o contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    } else {
                        // Recuperar el CI y nombre del empleado
                        val empleado = querySnapshot.documents[0]
                        val empleadoCi = empleado.getString("ci") ?: ""
                        val empleadoNombre = empleado.getString("nombre") ?: ""

                        // Guardar los datos del empleado para usarlos más tarde
                        // (puedes guardarlos en SharedPreferences o pasarlos por la actividad)
                        val intent = Intent(this, FacturacionActivity::class.java).apply {
                            putExtra("empleadoCi", empleadoCi)
                            putExtra("empleadoNombre", empleadoNombre)
                        }
                        startActivity(intent)
                        finish()  // Cerrar la actividad de login
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al intentar iniciar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
        }
    }

}
