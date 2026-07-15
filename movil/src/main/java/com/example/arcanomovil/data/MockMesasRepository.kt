package com.example.arcanomovil.data

object MockMesasRepository {

    private val mesas = mutableListOf(
        Mesa("1", 1, EstadoMesa.Ocupada),
        Mesa("2", 2, EstadoMesa.Ocupada),
        Mesa("3", 3, EstadoMesa.Disponible),
        Mesa("4", 4, EstadoMesa.Ocupada),
        Mesa("5", 5, EstadoMesa.Reservada),
        Mesa("6", 6, EstadoMesa.Disponible),
        Mesa("7", 7, EstadoMesa.Ocupada),
        Mesa("8", 8, EstadoMesa.Disponible)
    )

    fun todas(): List<Mesa> = mesas.sortedBy { it.numero }

    fun disponibles(): List<Mesa> = todas().filter { it.estado == EstadoMesa.Disponible }

    fun getByNumero(numero: Int): Mesa? = mesas.find { it.numero == numero }

    fun setEstadoByNumero(numero: Int, estado: EstadoMesa) {
        val index = mesas.indexOfFirst { it.numero == numero }
        if (index >= 0) {
            mesas[index] = mesas[index].copy(estado = estado)
        }
    }

    fun cicloEstado(id: String) {
        val index = mesas.indexOfFirst { it.id == id }
        if (index < 0) return
        val mesa = mesas[index]
        val siguiente = when (mesa.estado) {
            EstadoMesa.Disponible -> EstadoMesa.Ocupada
            EstadoMesa.Ocupada -> EstadoMesa.Reservada
            EstadoMesa.Reservada -> EstadoMesa.Disponible
        }
        mesas[index] = mesa.copy(estado = siguiente)
    }

    fun agregar() {
        val next = (mesas.maxOfOrNull { it.numero } ?: 0) + 1
        mesas.add(Mesa(next.toString(), next, EstadoMesa.Disponible))
    }
}
