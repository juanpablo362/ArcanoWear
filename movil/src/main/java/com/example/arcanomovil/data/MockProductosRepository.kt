package com.example.arcanomovil.data

object MockProductosRepository {
    fun menu(): List<ProductoMenu> = listOf(
        ProductoMenu("p1", "Pizza Margarita", 9.50),
        ProductoMenu("p2", "Pizza Pepperoni", 11.00),
        ProductoMenu("p3", "Pizza Hawaiana", 10.50),
        ProductoMenu("p4", "Ensalada César", 7.00),
        ProductoMenu("p5", "Limonada Natural", 3.50),
        ProductoMenu("p6", "Refresco", 2.50)
    )
}
