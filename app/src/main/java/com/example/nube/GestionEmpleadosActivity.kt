package com.example.nube

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GestionEmpleadosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var empleadosAdapter: EmpleadosAdapter
    private val empleadosList = mutableListOf<Empleado>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_empleados)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerViewEmpleados)
        recyclerView.layoutManager = LinearLayoutManager(this)

        empleadosAdapter = EmpleadosAdapter(empleadosList, ::editEmpleado, ::deleteEmpleado)
        recyclerView.adapter = empleadosAdapter

        val btnAddEmpleado: Button = findViewById(R.id.btnAddEmpleado)
        btnAddEmpleado.setOnClickListener { addEmpleado() }

        loadEmpleados()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadEmpleados() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        db.collection("users")
            .document(userId)
            .collection("empleados")
            .get()
            .addOnSuccessListener { querySnapshot ->
                empleadosList.clear()
                for (document in querySnapshot) {
                    val empleado = Empleado(
                        ci = document.getString("ci") ?: "",
                        nombre = document.getString("nombre") ?: "",
                        apellido = document.getString("apellido") ?: "",
                        telefono = document.getString("telefono") ?: "",
                        direccion = document.getString("direccion") ?: "",
                        cargo = document.getString("cargo") ?: "",
                        edad = document.getLong("edad")?.toInt() ?: 0
                    )
                    empleadosList.add(empleado)
                }
                empleadosAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar empleados: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun addEmpleado() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_empleado, null)
        val ciInput = view.findViewById<EditText>(R.id.inputCi)
        val nombreInput = view.findViewById<EditText>(R.id.inputNombre)
        val apellidoInput = view.findViewById<EditText>(R.id.inputApellido)
        val telefonoInput = view.findViewById<EditText>(R.id.inputTelefono)
        val direccionInput = view.findViewById<EditText>(R.id.inputDireccion)
        val cargoSpinner = view.findViewById<Spinner>(R.id.inputCargoSpinner)
        val edadInput = view.findViewById<EditText>(R.id.inputEdad)
        val passwordInput = view.findViewById<EditText>(R.id.inputPassword)

        val cargos = listOf("Gerente", "Cajero", "Recepcionista", "Supervisor")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cargos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cargoSpinner.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Añadir Empleado")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val ci = ciInput.text.toString()
                val nombre = nombreInput.text.toString()
                val apellido = apellidoInput.text.toString()
                val telefono = telefonoInput.text.toString()
                val direccion = direccionInput.text.toString()
                val cargo = cargoSpinner.selectedItem.toString()
                val edad = edadInput.text.toString().toIntOrNull()
                val password = passwordInput.text.toString()

                if (ci.isNotBlank() && nombre.isNotBlank() && apellido.isNotBlank() &&
                    telefono.isNotBlank() && direccion.isNotBlank() &&
                    cargo.isNotBlank() && edad != null && password.isNotBlank()
                ) {
                    if (cargo == "Gerente") {
                        db.collection("users")
                            .document(userId)
                            .collection("empleados")
                            .whereEqualTo("cargo", "Gerente")
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.size() < 2) {
                                    addEmpleadoToDatabase(
                                        userId, ci, nombre, apellido,
                                        telefono, direccion, cargo, edad, password
                                    )
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Solo se permiten 2 gerentes como máximo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    } else {
                        addEmpleadoToDatabase(
                            userId,
                            ci,
                            nombre,
                            apellido,
                            telefono,
                            direccion,
                            cargo,
                            edad,
                            password
                        )
                    }
                } else {
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

    private fun addEmpleadoToDatabase(
        userId: String,
        ci: String,
        nombre: String,
        apellido: String,
        telefono: String,
        direccion: String,
        cargo: String,
        edad: Int,
        password: String
    ) {
        // Eliminar la encriptación de la contraseña
        val empleado = hashMapOf(
            "ci" to ci,
            "nombre" to nombre,
            "apellido" to apellido,
            "telefono" to telefono,
            "direccion" to direccion,
            "cargo" to cargo,
            "edad" to edad,
            "password" to password // Aquí se guarda la contraseña sin encriptar
        )
        db.collection("users")
            .document(userId)
            .collection("empleados")
            .add(empleado)
            .addOnSuccessListener {
                Toast.makeText(this, "Empleado añadido exitosamente", Toast.LENGTH_SHORT).show()
                loadEmpleados()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al añadir empleado: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun editEmpleado(empleado: Empleado) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_empleado, null)
        val ciInput = view.findViewById<EditText>(R.id.inputCi)
        val nombreInput = view.findViewById<EditText>(R.id.inputNombre)
        val apellidoInput = view.findViewById<EditText>(R.id.inputApellido)
        val telefonoInput = view.findViewById<EditText>(R.id.inputTelefono)
        val direccionInput = view.findViewById<EditText>(R.id.inputDireccion)
        val cargoSpinner = view.findViewById<Spinner>(R.id.inputCargoSpinner)
        val edadInput = view.findViewById<EditText>(R.id.inputEdad)
        val passwordInput = view.findViewById<EditText>(R.id.inputPassword)

        val cargos = listOf("Gerente", "Cajero", "Recepcionista", "Supervisor")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cargos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cargoSpinner.adapter = adapter

        val currentCargoIndex = cargos.indexOf(empleado.cargo)
        if (currentCargoIndex != -1) {
            cargoSpinner.setSelection(currentCargoIndex)
        }

        ciInput.setText(empleado.ci)
        nombreInput.setText(empleado.nombre)
        apellidoInput.setText(empleado.apellido)
        telefonoInput.setText(empleado.telefono)
        direccionInput.setText(empleado.direccion)
        edadInput.setText(empleado.edad.toString())
        passwordInput.setText("") // No mostramos la contraseña actual por razones de seguridad

        AlertDialog.Builder(this)
            .setTitle("Editar Empleado")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val ci = ciInput.text.toString()
                val nombre = nombreInput.text.toString()
                val apellido = apellidoInput.text.toString()
                val telefono = telefonoInput.text.toString()
                val direccion = direccionInput.text.toString()
                val cargo = cargoSpinner.selectedItem.toString()
                val edad = edadInput.text.toString().toIntOrNull()
                val password = passwordInput.text.toString()

                if (ci.isNotBlank() && nombre.isNotBlank() && apellido.isNotBlank() &&
                    telefono.isNotBlank() && direccion.isNotBlank() &&
                    cargo.isNotBlank() && edad != null
                ) {
                    val updates = mutableMapOf<String, Any>(
                        "ci" to ci,
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "telefono" to telefono,
                        "direccion" to direccion,
                        "cargo" to cargo,
                        "edad" to edad
                    )
                    if (password.isNotBlank()) {
                        updates["password"] = password.hashCode().toString()
                    }

                    db.collection("users")
                        .document(userId)
                        .collection("empleados")
                        .whereEqualTo("ci", empleado.ci)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot) {
                                db.collection("users")
                                    .document(userId)
                                    .collection("empleados")
                                    .document(document.id)
                                    .update(updates)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Empleado actualizado exitosamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadEmpleados()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Error al actualizar empleado: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                } else {
                    Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }
    private fun deleteEmpleado(empleado: Empleado) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no logueado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        // Show confirmation dialog before deleting the employee
        AlertDialog.Builder(this)
            .setTitle("Eliminar Empleado")
            .setMessage("¿Estás seguro de eliminar a este empleado?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("users")
                    .document(userId)
                    .collection("empleados")
                    .whereEqualTo("ci", empleado.ci)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot) {
                            db.collection("users")
                                .document(userId)
                                .collection("empleados")
                                .document(document.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Empleado eliminado exitosamente", Toast.LENGTH_SHORT).show()
                                    loadEmpleados()  // Reload the list of employees
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al eliminar empleado: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al encontrar empleado: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }

}