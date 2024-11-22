package com.example.nube

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearArticuloActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_articulo)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: return
        val etNombre: EditText = findViewById(R.id.etNombre)
        val etCod: EditText = findViewById(R.id.etCod)
        val etCantidad: EditText = findViewById(R.id.etCantidad)
        val etPrecioUnitario: EditText = findViewById(R.id.etPrecioUnitario)
        val etTipo: EditText = findViewById(R.id.etTipo)
        val etDetalle: EditText = findViewById(R.id.etDetalle)
        val btnGuardar: Button = findViewById(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val cod = etCod.text.toString()
            val cantidad = etCantidad.text.toString().toIntOrNull()
            val precioUnitario = etPrecioUnitario.text.toString().toDoubleOrNull()
            val tipo = etTipo.text.toString()
            val detalle = etDetalle.text.toString()

            if (nombre.isEmpty() || cod.isEmpty() || cantidad == null || precioUnitario == null || tipo.isEmpty() || detalle.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val articulo = hashMapOf(
                "nombre" to nombre,
                "cod" to cod,
                "cantidad" to cantidad,
                "precioUnitario" to precioUnitario,
                "tipo" to tipo,
                "detalle" to detalle
            )

            db.collection("users").document(userId).collection("articulos").add(articulo)
                .addOnSuccessListener {
                    Toast.makeText(this, "Artículo creado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar el artículo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
