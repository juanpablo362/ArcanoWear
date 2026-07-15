package com.example.arcanomovil.data

enum class Rol {
    Administrador,
    Despachador,
    Operador
}

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: Rol,
    val activo: Boolean = true
) {
    fun iniciales(): String {
        val parts = nombre.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "?"
        }
    }
}

enum class EstadoMesa {
    Disponible,
    Ocupada,
    Reservada
}

data class Mesa(
    val id: String,
    val numero: Int,
    val estado: EstadoMesa
)

enum class EstadoOrdenPhone {
    Pendiente,
    EnPreparacion,
    Listo,
    Recogida,
    Entregado,
    Pagado,
    Cancelado
}

data class LineaOrden(
    val cantidad: Int,
    val nombre: String,
    val precioUnitario: Double
) {
    fun subtotal(): Double = cantidad * precioUnitario
}

data class ProductoMenu(
    val id: String,
    val nombre: String,
    val precio: Double
)

enum class MetodoPago {
    Efectivo,
    Tarjeta
}

data class OrdenPhone(
    val id: String,
    val numeroOrden: Int,
    val mesa: Int,
    val personas: Int,
    val estado: EstadoOrdenPhone,
    val total: Double,
    val fecha: String,
    val hora: String,
    val productos: List<LineaOrden>
)
