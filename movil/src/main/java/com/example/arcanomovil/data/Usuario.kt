package com.example.arcanomovil.data

enum class Rol {
    Administrador,
    Despachador,
    Operador
}

data class Usuario(
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: Rol
)
