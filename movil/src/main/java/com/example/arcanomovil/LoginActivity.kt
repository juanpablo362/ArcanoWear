package com.example.arcanomovil

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.arcanomovil.data.AuthApiClient
import com.example.arcanomovil.data.LoginResult
import com.example.arcanomovil.data.SessionRepository

class LoginActivity : AppCompatActivity() {

    private var passwordVisible = false
    private var cargando = false

    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvError: TextView
    private lateinit var btnLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        tvError = findViewById(R.id.tvError)
        btnLogin = findViewById(R.id.btnLogin)
        val btnTogglePassword = findViewById<ImageView>(R.id.btnTogglePassword)

        btnTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            etPassword.inputType = if (passwordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            intentarLogin()
        }
    }

    private fun intentarLogin() {
        if (cargando) return

        val correo = etCorreo.text.toString()
        val password = etPassword.text.toString()
        if (correo.isBlank() || password.isBlank()) {
            mostrarError(getString(R.string.login_campos_obligatorios))
            return
        }

        setCargando(true)
        AuthApiClient.loginAsync(correo, password) { result ->
            runOnUiThread {
                setCargando(false)
                when (result) {
                    is LoginResult.Ok -> {
                        SessionRepository.guardar(result.sesion)
                        val usuario = result.sesion.usuario
                        startActivity(
                            Intent(this, ShellActivity::class.java).apply {
                                putExtra(SessionExtras.NOMBRE, usuario.nombre)
                                putExtra(SessionExtras.CORREO, usuario.correo)
                                putExtra(SessionExtras.ROL, usuario.rol.name)
                            }
                        )
                        finish()
                    }
                    is LoginResult.Error -> mostrarError(result.mensaje)
                }
            }
        }
    }

    private fun setCargando(activo: Boolean) {
        cargando = activo
        btnLogin.isEnabled = !activo
        btnLogin.alpha = if (activo) 0.6f else 1f
        btnLogin.text = getString(
            if (activo) R.string.btn_login_loading else R.string.btn_login
        )
        if (activo) tvError.visibility = View.GONE
    }

    private fun mostrarError(mensaje: String) {
        tvError.text = mensaje
        tvError.visibility = View.VISIBLE
    }
}
