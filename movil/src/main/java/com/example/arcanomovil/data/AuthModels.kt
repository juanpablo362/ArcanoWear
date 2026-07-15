package com.example.arcanomovil.data

data class SesionAuth(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val usuario: Usuario
)

sealed class LoginResult {
    data class Ok(val sesion: SesionAuth) : LoginResult()
    data class Error(val mensaje: String) : LoginResult()
}
