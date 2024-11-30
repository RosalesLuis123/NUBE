package com.example.nube
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class GestionPersonasActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_personas)

        val btnClientes: Button = findViewById(R.id.btnClientes)
        val btnEmpleados: Button = findViewById(R.id.btnEmpleados)

        btnClientes.setOnClickListener {
            val intent = Intent(this, GestionClientesActivity::class.java)
            startActivity(intent)
        }

        btnEmpleados.setOnClickListener {
            val intent = Intent(this, GestionEmpleadosActivity::class.java)
            startActivity(intent)
        }
    }
}
