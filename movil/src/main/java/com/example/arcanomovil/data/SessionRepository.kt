package com.example.arcanomovil.data

/**
 * Sesión real (API) para login. El resto de pantallas sigue con datos mock.
 */
object SessionRepository {

    var sesion: SesionAuth? = null
        private set

    val usuarioActual: Usuario?
        get() = sesion?.usuario

    val accessToken: String?
        get() = sesion?.accessToken

    fun guardar(sesionAuth: SesionAuth) {
        sesion = sesionAuth
    }

    fun logout() {
        sesion = null
    }
}
