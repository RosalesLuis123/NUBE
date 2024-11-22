package com.example.nube

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearEmpresaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_empresa)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val etNombre: EditText = findViewById(R.id.etNombre)
        val etDireccion: EditText = findViewById(R.id.etDireccion)
        val etNIT: EditText = findViewById(R.id.etNIT)
        val etDatos: EditText = findViewById(R.id.etDatos)
        val etTelefono: EditText = findViewById(R.id.etTelefono)
        val etRubro: EditText = findViewById(R.id.etRubro)
        val btnGuardar: Button = findViewById(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val direccion = etDireccion.text.toString()
            val nit = etNIT.text.toString()
            val datos = etDatos.text.toString()
            val telefono = etTelefono.text.toString()
            val rubro = etRubro.text.toString()

            if (nombre.isEmpty() || direccion.isEmpty() || nit.isEmpty() || datos.isEmpty() || telefono.isEmpty() || rubro.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val empresa = hashMapOf(
                "nombre" to nombre,
                "direccion" to direccion,
                "NIT" to nit,
                "datos" to datos,
                "telefono" to telefono,
                "rubro" to rubro
            )

            db.collection("users").document(userId).collection("empresas").add(empresa)
                .addOnSuccessListener {
                    Toast.makeText(this, "Empresa creada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar la empresa: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
