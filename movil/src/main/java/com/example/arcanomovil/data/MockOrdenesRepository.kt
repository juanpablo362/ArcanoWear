package com.example.arcanomovil.data

object MockOrdenesRepository {

    private val ordenes = mutableListOf(
        OrdenPhone("1", 1260, 4, EstadoOrdenPhone.Listo, 38.50, "13 jul. 2026, 14:10"),
        OrdenPhone("2", 1261, 7, EstadoOrdenPhone.Recogida, 52.00, "13 jul. 2026, 14:22"),
        OrdenPhone("3", 1262, 2, EstadoOrdenPhone.EnPreparacion, 27.90, "13 jul. 2026, 14:28"),
        OrdenPhone("4", 1258, 12, EstadoOrdenPhone.Entregado, 45.80, "24 may. 2025, 14:35"),
        OrdenPhone("5", 1257, 3, EstadoOrdenPhone.Pagado, 62.40, "24 may. 2025, 13:50"),
        OrdenPhone("6", 1256, 8, EstadoOrdenPhone.Entregado, 29.00, "24 may. 2025, 13:12"),
        OrdenPhone("7", 1255, 1, EstadoOrdenPhone.Pagado, 74.20, "23 may. 2025, 21:05"),
        OrdenPhone("8", 1254, 5, EstadoOrdenPhone.Cancelado, 18.50, "23 may. 2025, 20:40")
    )

    fun activas(): List<OrdenPhone> = ordenes.filter {
        it.estado == EstadoOrdenPhone.Pendiente ||
            it.estado == EstadoOrdenPhone.EnPreparacion ||
            it.estado == EstadoOrdenPhone.Listo ||
            it.estado == EstadoOrdenPhone.Recogida
    }

    fun historial(): List<OrdenPhone> = ordenes.filter {
        it.estado == EstadoOrdenPhone.Entregado ||
            it.estado == EstadoOrdenPhone.Pagado ||
            it.estado == EstadoOrdenPhone.Cancelado
    }

    fun buscarHistorial(query: String): List<OrdenPhone> {
        val q = query.trim()
        if (q.isEmpty()) return historial()
        return historial().filter {
            it.mesa.toString().contains(q) ||
                it.numeroOrden.toString().contains(q)
        }
    }
}
