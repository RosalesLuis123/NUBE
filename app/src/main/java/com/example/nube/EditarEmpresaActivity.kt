package com.example.nube

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarEmpresaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var empresaId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_empresa)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        empresaId = intent.getStringExtra("empresaId") ?: return

        val etNombre: EditText = findViewById(R.id.etNombre)
        val etDireccion: EditText = findViewById(R.id.etDireccion)
        val etNIT: EditText = findViewById(R.id.etNIT)
        val etDatos: EditText = findViewById(R.id.etDatos)
        val etTelefono: EditText = findViewById(R.id.etTelefono)
        val etRubro: EditText = findViewById(R.id.etRubro)
        val btnGuardar: Button = findViewById(R.id.btnGuardar)

        // Cargar datos de la empresa
        db.collection("users").document(userId).collection("empresas").document(empresaId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etNombre.setText(document.getString("nombre"))
                    etDireccion.setText(document.getString("direccion"))
                    etNIT.setText(document.getString("NIT"))
                    etDatos.setText(document.getString("datos"))
                    etTelefono.setText(document.getString("telefono"))
                    etRubro.setText(document.getString("rubro"))
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar la empresa: ${e.message}", Toast.LENGTH_SHORT).show()
            }

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

            val empresaActualizada = hashMapOf(
                "nombre" to nombre,
                "direccion" to direccion,
                "NIT" to nit,
                "datos" to datos,
                "telefono" to telefono,
                "rubro" to rubro
            )

            db.collection("users").document(userId).collection("empresas").document(empresaId).set(empresaActualizada)
                .addOnSuccessListener {
                    Toast.makeText(this, "Empresa actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar la empresa: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
