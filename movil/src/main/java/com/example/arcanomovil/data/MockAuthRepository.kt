package com.example.arcanomovil.data

object MockAuthRepository {

    private val usuarios = listOf(
        Usuario(
            nombre = "Andrea Méndez",
            correo = "admin@arcano.com",
            password = "admin",
            rol = Rol.Administrador
        ),
        Usuario(
            nombre = "Carlos Ruiz",
            correo = "despacho@arcano.com",
            password = "despacho",
            rol = Rol.Despachador
        ),
        Usuario(
            nombre = "Luis Ortega",
            correo = "operador@arcano.com",
            password = "operador",
            rol = Rol.Operador
        )
    )

    var sesionActual: Usuario? = null
        private set

    fun login(correo: String, password: String): Usuario? {
        val usuario = usuarios.find {
            it.correo.equals(correo.trim(), ignoreCase = true) && it.password == password
        }
        sesionActual = usuario
        return usuario
    }

    fun logout() {
        sesionActual = null
    }
}
