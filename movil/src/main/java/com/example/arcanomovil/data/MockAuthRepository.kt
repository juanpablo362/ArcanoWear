package com.example.arcanomovil.data

object MockAuthRepository {

    var sesionActual: Usuario? = null
        private set

    fun login(correo: String, password: String): Usuario? {
        val usuario = MockUsuariosRepository.login(correo, password)
        sesionActual = usuario
        return usuario
    }

    fun logout() {
        sesionActual = null
    }
}
