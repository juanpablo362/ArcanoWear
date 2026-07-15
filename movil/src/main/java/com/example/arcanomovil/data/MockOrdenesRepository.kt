package com.example.arcanomovil.data

object MockOrdenesRepository {

    private val ordenes = mutableListOf(
        OrdenPhone(
            id = "1",
            numeroOrden = 1260,
            mesa = 4,
            personas = 2,
            estado = EstadoOrdenPhone.Listo,
            total = 38.50,
            fecha = "13 jul. 2026, 14:10",
            hora = "2:10 p. m.",
            productos = listOf(
                LineaOrden(2, "Pizza Pepperoni", 11.00),
                LineaOrden(2, "Refresco", 2.50),
                LineaOrden(1, "Ensalada César", 7.00)
            )
        ),
        OrdenPhone(
            id = "2",
            numeroOrden = 1261,
            mesa = 7,
            personas = 3,
            estado = EstadoOrdenPhone.Recogida,
            total = 33.00,
            fecha = "13 jul. 2026, 14:22",
            hora = "2:22 p. m.",
            productos = listOf(
                LineaOrden(1, "Pizza Margarita", 9.50),
                LineaOrden(1, "Pizza Hawaiana", 10.50),
                LineaOrden(2, "Limonada Natural", 3.50),
                LineaOrden(1, "Ensalada César", 7.00)
            )
        ),
        OrdenPhone(
            id = "3",
            numeroOrden = 1262,
            mesa = 2,
            personas = 2,
            estado = EstadoOrdenPhone.EnPreparacion,
            total = 27.90,
            fecha = "13 jul. 2026, 14:28",
            hora = "2:28 p. m.",
            productos = listOf(
                LineaOrden(1, "Pizza Pepperoni", 11.00),
                LineaOrden(1, "Pizza Margarita", 9.50),
                LineaOrden(2, "Refresco", 2.50)
            )
        ),
        OrdenPhone(
            id = "9",
            numeroOrden = 1263,
            mesa = 1,
            personas = 4,
            estado = EstadoOrdenPhone.Pendiente,
            total = 41.00,
            fecha = "13 jul. 2026, 14:35",
            hora = "2:35 p. m.",
            productos = listOf(
                LineaOrden(1, "Ensalada César", 7.00),
                LineaOrden(1, "Limonada Natural", 3.50),
                LineaOrden(2, "Pizza Hawaiana", 10.50),
                LineaOrden(1, "Pizza Pepperoni", 11.00)
            )
        ),
        OrdenPhone(
            id = "10",
            numeroOrden = 1264,
            mesa = 7,
            personas = 2,
            estado = EstadoOrdenPhone.Entregado,
            total = 33.00,
            fecha = "13 jul. 2026, 13:50",
            hora = "1:50 p. m.",
            productos = listOf(
                LineaOrden(1, "Pizza Margarita", 9.50),
                LineaOrden(1, "Pizza Hawaiana", 10.50),
                LineaOrden(2, "Limonada Natural", 3.50),
                LineaOrden(1, "Ensalada César", 7.00)
            )
        ),
        OrdenPhone(
            id = "4",
            numeroOrden = 1258,
            mesa = 12,
            personas = 2,
            estado = EstadoOrdenPhone.Entregado,
            total = 45.80,
            fecha = "24 may. 2025, 14:35",
            hora = "2:35 p. m.",
            productos = listOf(LineaOrden(2, "Pizza Pepperoni", 11.00))
        ),
        OrdenPhone(
            id = "5",
            numeroOrden = 1257,
            mesa = 3,
            personas = 3,
            estado = EstadoOrdenPhone.Pagado,
            total = 62.40,
            fecha = "24 may. 2025, 13:50",
            hora = "1:50 p. m.",
            productos = listOf(LineaOrden(3, "Pizza Margarita", 9.50))
        ),
        OrdenPhone(
            id = "6",
            numeroOrden = 1256,
            mesa = 8,
            personas = 1,
            estado = EstadoOrdenPhone.Entregado,
            total = 29.00,
            fecha = "24 may. 2025, 13:12",
            hora = "1:12 p. m.",
            productos = listOf(LineaOrden(1, "Pizza Hawaiana", 10.50))
        ),
        OrdenPhone(
            id = "7",
            numeroOrden = 1255,
            mesa = 1,
            personas = 4,
            estado = EstadoOrdenPhone.Pagado,
            total = 74.20,
            fecha = "23 may. 2025, 21:05",
            hora = "9:05 p. m.",
            productos = listOf(LineaOrden(4, "Pizza Pepperoni", 11.00))
        ),
        OrdenPhone(
            id = "8",
            numeroOrden = 1254,
            mesa = 5,
            personas = 2,
            estado = EstadoOrdenPhone.Cancelado,
            total = 18.50,
            fecha = "23 may. 2025, 20:40",
            hora = "8:40 p. m.",
            productos = listOf(LineaOrden(1, "Pizza Margarita", 9.50))
        )
    )

    fun todas(): List<OrdenPhone> = ordenes.toList()

    fun activas(): List<OrdenPhone> = ordenes.filter {
        it.estado == EstadoOrdenPhone.Pendiente ||
            it.estado == EstadoOrdenPhone.EnPreparacion ||
            it.estado == EstadoOrdenPhone.Listo ||
            it.estado == EstadoOrdenPhone.Recogida
    }

    fun gestionDespacho(filtro: EstadoOrdenPhone?): List<OrdenPhone> {
        val base = ordenes.filter {
            it.estado == EstadoOrdenPhone.Pendiente ||
                it.estado == EstadoOrdenPhone.EnPreparacion ||
                it.estado == EstadoOrdenPhone.Listo
        }
        return if (filtro == null) base else base.filter { it.estado == filtro }
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

    fun mesasConCuentaPendiente(): List<Int> =
        ordenes.filter { it.estado == EstadoOrdenPhone.Entregado }
            .map { it.mesa }
            .distinct()
            .sorted()

    fun cuentaMesa(mesa: Int): List<OrdenPhone> =
        ordenes.filter { it.mesa == mesa && it.estado == EstadoOrdenPhone.Entregado }

    fun avanceEstado(id: String): Boolean {
        val index = ordenes.indexOfFirst { it.id == id }
        if (index < 0) return false
        val orden = ordenes[index]
        val siguiente = when (orden.estado) {
            EstadoOrdenPhone.Pendiente -> EstadoOrdenPhone.EnPreparacion
            EstadoOrdenPhone.EnPreparacion -> EstadoOrdenPhone.Listo
            else -> return false
        }
        ordenes[index] = orden.copy(estado = siguiente)
        return true
    }

    fun registrar(
        mesa: Int,
        personas: Int,
        productos: List<LineaOrden>
    ): OrdenPhone {
        val total = productos.sumOf { it.subtotal() }
        val nextNum = (ordenes.maxOfOrNull { it.numeroOrden } ?: 1200) + 1
        val orden = OrdenPhone(
            id = nextNum.toString(),
            numeroOrden = nextNum,
            mesa = mesa,
            personas = personas,
            estado = EstadoOrdenPhone.Pendiente,
            total = total,
            fecha = "13 jul. 2026, ahora",
            hora = "ahora",
            productos = productos
        )
        ordenes.add(0, orden)
        MockMesasRepository.setEstadoByNumero(mesa, EstadoMesa.Ocupada)
        return orden
    }

    fun confirmarPagoMesa(mesa: Int, metodo: MetodoPago): Boolean {
        val pendientes = cuentaMesa(mesa)
        if (pendientes.isEmpty()) return false
        pendientes.forEach { orden ->
            val index = ordenes.indexOfFirst { it.id == orden.id }
            if (index >= 0) {
                ordenes[index] = ordenes[index].copy(estado = EstadoOrdenPhone.Pagado)
            }
        }
        MockMesasRepository.setEstadoByNumero(mesa, EstadoMesa.Disponible)
        return true
    }
}
