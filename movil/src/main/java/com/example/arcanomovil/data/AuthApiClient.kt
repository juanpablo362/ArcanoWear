package com.example.arcanomovil.data

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

object AuthApiClient {

    private val executor = Executors.newSingleThreadExecutor()

    fun loginAsync(
        correo: String,
        password: String,
        onResult: (LoginResult) -> Unit
    ) {
        executor.execute {
            val result = login(correo, password)
            onResult(result)
        }
    }

    fun login(correo: String, password: String): LoginResult {
        val correoLimpio = correo.trim()
        if (correoLimpio.isEmpty() || password.isEmpty()) {
            return LoginResult.Error("Correo y contraseña son obligatorios.")
        }

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(ApiConfig.LOGIN_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val body = JSONObject()
                .put("correo", correoLimpio)
                .put("password", password)
                .toString()

            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(body)
                writer.flush()
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val raw = stream?.let { readBody(it) }.orEmpty()

            when (code) {
                200 -> parseSuccess(raw)
                400 -> LoginResult.Error(parseMensaje(raw) ?: "Correo y contraseña son obligatorios.")
                401 -> LoginResult.Error("Correo o contraseña incorrectos.")
                else -> LoginResult.Error("Error del servidor ($code). Intenta de nuevo.")
            }
        } catch (e: Exception) {
            LoginResult.Error("Sin conexión con la API. Revisa internet e intenta de nuevo.")
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseSuccess(raw: String): LoginResult {
        return try {
            val json = JSONObject(raw)
            val usuarioJson = json.getJSONObject("usuario")
            val rolApi = usuarioJson.getString("rol")
            val rol = mapRol(rolApi)
                ?: return LoginResult.Error(
                    "Tu rol ($rolApi) no tiene acceso a Service Wear. Usa Administrador, Despachador u Operador."
                )

            val usuario = Usuario(
                id = usuarioJson.getInt("idUsuario").toString(),
                nombre = usuarioJson.getString("nombreUsuario"),
                correo = usuarioJson.getString("correo"),
                password = "",
                rol = rol,
                activo = true
            )

            LoginResult.Ok(
                SesionAuth(
                    accessToken = json.getString("accessToken"),
                    refreshToken = json.getString("refreshToken"),
                    expiresIn = json.optInt("expiresIn", 1800),
                    usuario = usuario
                )
            )
        } catch (e: Exception) {
            LoginResult.Error("Respuesta inválida del servidor.")
        }
    }

    private fun mapRol(rolApi: String): Rol? = when (rolApi.trim().lowercase()) {
        "administrador" -> Rol.Administrador
        "despachador" -> Rol.Despachador
        "operador" -> Rol.Operador
        else -> null
    }

    private fun parseMensaje(raw: String): String? {
        if (raw.isBlank()) return null
        return try {
            val json = JSONObject(raw)
            when {
                json.has("mensaje") -> json.optString("mensaje").ifBlank { null }
                json.has("title") -> json.optString("title").ifBlank { null }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readBody(stream: java.io.InputStream): String {
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
            buildString {
                var line = reader.readLine()
                while (line != null) {
                    append(line)
                    line = reader.readLine()
                }
            }
        }
    }
}
