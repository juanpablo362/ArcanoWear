package com.example.arcanomovil

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.arcanomovil.data.MockAuthRepository

class LoginActivity : AppCompatActivity() {

    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvError = findViewById<TextView>(R.id.tvError)
        val btnLogin = findViewById<TextView>(R.id.btnLogin)
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
            val usuario = MockAuthRepository.login(
                etCorreo.text.toString(),
                etPassword.text.toString()
            )
            if (usuario == null) {
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE
            val intent = Intent(this, ShellActivity::class.java).apply {
                putExtra(SessionExtras.NOMBRE, usuario.nombre)
                putExtra(SessionExtras.CORREO, usuario.correo)
                putExtra(SessionExtras.ROL, usuario.rol.name)
            }
            startActivity(intent)
            finish()
        }
    }
}
