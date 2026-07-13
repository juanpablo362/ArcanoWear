package com.example.arcanowear.data

object MockOrdenesRepository {

    private val ordenes = mutableListOf(
        Orden(
            id = "1",
            mesa = 4,
            estado = EstadoOrden.Listo,
            productos = listOf(LineaProducto(cantidad = 2, nombre = "Pepperoni"))
        ),
        Orden(
            id = "2",
            mesa = 7,
            estado = EstadoOrden.Recogida,
            productos = listOf(
                LineaProducto(cantidad = 1, nombre = "Hawaiana"),
                LineaProducto(cantidad = 1, nombre = "Refresco")
            )
        ),
        Orden(
            id = "3",
            mesa = 2,
            estado = EstadoOrden.Listo,
            productos = listOf(
                LineaProducto(cantidad = 1, nombre = "Margarita"),
                LineaProducto(cantidad = 2, nombre = "Agua")
            )
        )
    )

    fun pendientes(): List<Orden> =
        ordenes.filter {
            it.estado == EstadoOrden.Listo || it.estado == EstadoOrden.Recogida
        }

    fun getById(id: String): Orden? =
        ordenes.find { it.id == id }

    fun confirmarRecoleccion(id: String): Boolean {
        val index = ordenes.indexOfFirst { it.id == id }
        if (index < 0) return false
        val orden = ordenes[index]
        if (orden.estado != EstadoOrden.Listo) return false
        ordenes[index] = orden.copy(estado = EstadoOrden.Recogida)
        return true
    }

    fun confirmarEntrega(id: String): Boolean {
        val index = ordenes.indexOfFirst { it.id == id }
        if (index < 0) return false
        val orden = ordenes[index]
        if (orden.estado != EstadoOrden.Recogida) return false
        ordenes[index] = orden.copy(estado = EstadoOrden.Entregado)
        return true
    }
}
