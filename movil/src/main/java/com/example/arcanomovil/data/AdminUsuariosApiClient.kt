package com.example.arcanomovil.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

sealed class ApiResult<out T> {
    data class Ok<T>(val data: T) : ApiResult<T>()
    data class Error(val mensaje: String) : ApiResult<Nothing>()
}

data class UsuarioAdminApi(
    val id: Int,
    val nombre: String,
    val email: String,
    val telefono: String?,
    val tipo: String,
    val activo: Boolean
) {
    fun aUsuarioLocal(): Usuario {
        val rol = when (tipo.trim().lowercase()) {
            "administrador" -> Rol.Administrador
            "despachador" -> Rol.Despachador
            else -> Rol.Operador
        }
        return Usuario(
            id = id.toString(),
            nombre = nombre,
            correo = email,
            password = "",
            rol = rol,
            activo = activo
        )
    }
}

object AdminUsuariosApiClient {

    private val executor = Executors.newSingleThreadExecutor()

    fun listarAsync(onResult: (ApiResult<List<UsuarioAdminApi>>) -> Unit) {
        executor.execute { onResult(listar()) }
    }

    fun crearAsync(
        nombre: String,
        email: String,
        password: String,
        tipo: String,
        onResult: (ApiResult<UsuarioAdminApi>) -> Unit
    ) {
        executor.execute { onResult(crear(nombre, email, password, tipo)) }
    }

    fun actualizarAsync(
        id: Int,
        nombre: String,
        email: String,
        tipo: String,
        activo: Boolean,
        password: String?,
        onResult: (ApiResult<Unit>) -> Unit
    ) {
        executor.execute { onResult(actualizar(id, nombre, email, tipo, activo, password)) }
    }

    fun toggleAsync(id: Int, onResult: (ApiResult<Unit>) -> Unit) {
        executor.execute { onResult(toggle(id)) }
    }

    private fun listar(): ApiResult<List<UsuarioAdminApi>> {
        val token = SessionRepository.accessToken
            ?: return ApiResult.Error("Sesión expirada. Vuelve a iniciar sesión.")

        return try {
            val conn = open("GET", ApiConfig.USUARIOS_URL, token, hasBody = false)
            val code = conn.responseCode
            val raw = readResponse(conn)
            conn.disconnect()
            when (code) {
                200 -> {
                    val array = JSONArray(raw)
                    val list = mutableListOf<UsuarioAdminApi>()
                    for (i in 0 until array.length()) {
                        list.add(parseUsuario(array.getJSONObject(i)))
                    }
                    ApiResult.Ok(list)
                }
                401, 403 -> ApiResult.Error("No tienes permiso de administrador.")
                else -> ApiResult.Error(parseError(raw, code))
            }
        } catch (_: Exception) {
            ApiResult.Error("Sin conexión con la API.")
        }
    }

    private fun crear(
        nombre: String,
        email: String,
        password: String,
        tipo: String
    ): ApiResult<UsuarioAdminApi> {
        val token = SessionRepository.accessToken
            ?: return ApiResult.Error("Sesión expirada. Vuelve a iniciar sesión.")

        return try {
            val body = JSONObject()
                .put("nombre", nombre)
                .put("email", email)
                .put("tipo", tipo)
                .put("password", password)
                .toString()

            val conn = open("POST", ApiConfig.USUARIOS_URL, token, hasBody = true)
            writeBody(conn, body)
            val code = conn.responseCode
            val raw = readResponse(conn)
            conn.disconnect()
            when (code) {
                200, 201 -> ApiResult.Ok(parseUsuario(JSONObject(raw)))
                400 -> ApiResult.Error(parseError(raw, code))
                401, 403 -> ApiResult.Error("No tienes permiso de administrador.")
                else -> ApiResult.Error(parseError(raw, code))
            }
        } catch (_: Exception) {
            ApiResult.Error("Sin conexión con la API.")
        }
    }

    private fun actualizar(
        id: Int,
        nombre: String,
        email: String,
        tipo: String,
        activo: Boolean,
        password: String?
    ): ApiResult<Unit> {
        val token = SessionRepository.accessToken
            ?: return ApiResult.Error("Sesión expirada. Vuelve a iniciar sesión.")

        return try {
            val body = JSONObject()
                .put("nombre", nombre)
                .put("email", email)
                .put("tipo", tipo)
                .put("activo", activo)
            if (!password.isNullOrBlank()) {
                body.put("password", password)
            }

            val conn = open("PUT", "${ApiConfig.USUARIOS_URL}/$id", token, hasBody = true)
            writeBody(conn, body.toString())
            val code = conn.responseCode
            val raw = readResponse(conn)
            conn.disconnect()
            when (code) {
                204, 200 -> ApiResult.Ok(Unit)
                404 -> ApiResult.Error("Usuario no encontrado.")
                401, 403 -> ApiResult.Error("No tienes permiso de administrador.")
                else -> ApiResult.Error(parseError(raw, code))
            }
        } catch (_: Exception) {
            ApiResult.Error("Sin conexión con la API.")
        }
    }

    private fun toggle(id: Int): ApiResult<Unit> {
        val token = SessionRepository.accessToken
            ?: return ApiResult.Error("Sesión expirada. Vuelve a iniciar sesión.")

        return try {
            val conn = open("PATCH", "${ApiConfig.USUARIOS_URL}/$id/toggle", token, hasBody = false)
            val code = conn.responseCode
            val raw = readResponse(conn)
            conn.disconnect()
            when (code) {
                204, 200 -> ApiResult.Ok(Unit)
                404 -> ApiResult.Error("Usuario no encontrado.")
                401, 403 -> ApiResult.Error("No tienes permiso de administrador.")
                else -> ApiResult.Error(parseError(raw, code))
            }
        } catch (_: Exception) {
            ApiResult.Error("Sin conexión con la API.")
        }
    }

    private fun open(
        method: String,
        url: String,
        token: String,
        hasBody: Boolean
    ): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 20000
            doInput = true
            doOutput = hasBody
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            if (hasBody) {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
    }

    private fun writeBody(conn: HttpURLConnection, body: String) {
        OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use {
            it.write(body)
            it.flush()
        }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val stream: InputStream? = try {
            if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        } catch (_: Exception) {
            null
        }
        return stream?.let { readBody(it) }.orEmpty()
    }

    private fun readBody(stream: InputStream): String {
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

    private fun parseUsuario(json: JSONObject): UsuarioAdminApi {
        return UsuarioAdminApi(
            id = json.getInt("id"),
            nombre = json.getString("nombre"),
            email = json.getString("email"),
            telefono = if (json.isNull("telefono")) null else json.optString("telefono"),
            tipo = json.getString("tipo"),
            activo = json.optBoolean("activo", true)
        )
    }

    private fun parseError(raw: String, code: Int): String {
        if (raw.isBlank()) return "Error del servidor ($code)."
        return try {
            val json = JSONObject(raw)
            when {
                json.has("mensaje") -> json.optString("mensaje").ifBlank { "Error ($code)" }
                json.has("error") -> json.optString("error").ifBlank { "Error ($code)" }
                json.has("title") -> json.optString("title").ifBlank { "Error ($code)" }
                else -> "Error del servidor ($code)."
            }
        } catch (_: Exception) {
            "Error del servidor ($code)."
        }
    }
}
