package com.example.nube

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class EmpresaMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empresa_menu)

        val btnFacturacion: Button = findViewById(R.id.btnFacturacion)
        val btnGestion: Button = findViewById(R.id.btnGestion)
        val btnVentas: Button = findViewById(R.id.btnVentas)
        val btnInventarios: Button = findViewById(R.id.btnInventarios)

        // Configurar los clics de los botones
        btnFacturacion.setOnClickListener {
            // Implementar lógica para Facturación (Ejemplo: ir a FacturacionActivity)
            startActivity(Intent(this, FacturacionActivity::class.java))
        }

        btnGestion.setOnClickListener {
            // Implementar lógica para Gestión de Empresa
            startActivity(Intent(this, GestionEmpresaActivity::class.java))
        }

        btnVentas.setOnClickListener {
            // Implementar lógica para Ventas
            startActivity(Intent(this, VentasActivity::class.java))
        }

        btnInventarios.setOnClickListener {
            // Implementar lógica para Inventarios
            startActivity(Intent(this, InventariosActivity::class.java))
        }
    }
}
