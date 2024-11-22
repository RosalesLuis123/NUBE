package com.example.nube

data class Venta(
    val id: String,
    val articulos: List<Map<String, Any>>,
    val total: Double,
    val fecha: String
)
