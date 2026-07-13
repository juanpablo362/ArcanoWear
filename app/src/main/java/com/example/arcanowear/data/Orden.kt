package com.example.arcanowear.data

enum class EstadoOrden {
    Listo,
    Recogida,
    Entregado
}

data class LineaProducto(
    val cantidad: Int,
    val nombre: String
)

data class Orden(
    val id: String,
    val mesa: Int,
    val estado: EstadoOrden,
    val productos: List<LineaProducto>
)
