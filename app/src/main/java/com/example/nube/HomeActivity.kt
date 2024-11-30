package com.example.nube

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Botón para Empleado
        val btnEmpleado: Button = findViewById(R.id.btnEmpleado)
        btnEmpleado.setOnClickListener {
            // Redirige a la actividad de login para empleado
            val intent = Intent(this, LoginEmpleadoActivity::class.java)
            startActivity(intent)
        }

        // Botón para Dueño
        val btnDueno: Button = findViewById(R.id.btnDueno)
        btnDueno.setOnClickListener {
            // Redirige a la actividad de login para dueño
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }
}
