package com.example.arcanomovil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.arcanomovil.data.MockAuthRepository
import com.example.arcanomovil.data.Rol
import com.google.android.material.bottomnavigation.BottomNavigationView

class ShellActivity : AppCompatActivity() {

    private lateinit var contentContainer: FrameLayout
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var nombre: String
    private lateinit var correo: String
    private lateinit var rol: Rol

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shell)

        nombre = intent.getStringExtra(SessionExtras.NOMBRE).orEmpty()
        correo = intent.getStringExtra(SessionExtras.CORREO).orEmpty()
        rol = runCatching {
            Rol.valueOf(intent.getStringExtra(SessionExtras.ROL).orEmpty())
        }.getOrElse {
            finish()
            return
        }

        contentContainer = findViewById(R.id.contentContainer)
        bottomNav = findViewById(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    showInicio()
                    true
                }
                R.id.nav_ordenes -> {
                    showStub(getString(R.string.stub_ordenes), textoOrdenesPorRol())
                    true
                }
                R.id.nav_historial -> {
                    showStub(getString(R.string.stub_historial), "Consulta de órdenes procesadas (simulado).")
                    true
                }
                R.id.nav_cuenta -> {
                    showCuenta()
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.nav_inicio
    }

    private fun textoOrdenesPorRol(): String = when (rol) {
        Rol.Administrador -> "Vista general de órdenes del salón (simulado)."
        Rol.Despachador -> "Aquí gestionarás la cola hasta Listo."
        Rol.Operador -> "Aquí registrarás nuevas órdenes de mesa."
    }

    private fun showInicio() {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_inicio, contentContainer, false)

        val tvSaludo = view.findViewById<TextView>(R.id.tvSaludo)
        val tvRolChip = view.findViewById<TextView>(R.id.tvRolChip)
        val tvTitulo = view.findViewById<TextView>(R.id.tvInicioTitulo)
        val tvDesc = view.findViewById<TextView>(R.id.tvInicioDesc)
        val tvExtra = view.findViewById<TextView>(R.id.tvInicioExtra)
        val acciones = view.findViewById<LinearLayout>(R.id.accionesContainer)

        tvSaludo.text = getString(R.string.saludo_format, nombre.split(" ").first())

        when (rol) {
            Rol.Administrador -> {
                tvRolChip.text = getString(R.string.rol_admin)
                tvRolChip.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_admin_fg))
                tvRolChip.setBackgroundResource(R.drawable.bg_chip_admin)
                tvTitulo.text = getString(R.string.inicio_admin_title)
                tvDesc.text = getString(R.string.inicio_admin_desc)
                addAccion(acciones, getString(R.string.inicio_admin_action_usuarios))
                addAccion(acciones, getString(R.string.inicio_admin_action_mesas))
            }
            Rol.Despachador -> {
                tvRolChip.text = getString(R.string.rol_despachador)
                tvRolChip.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_desp_fg))
                tvRolChip.setBackgroundResource(R.drawable.bg_chip_desp)
                tvTitulo.text = getString(R.string.inicio_desp_title)
                tvDesc.text = getString(R.string.inicio_desp_desc)
                tvExtra.visibility = View.VISIBLE
                tvExtra.text = getString(R.string.inicio_desp_stat)
                addAccion(acciones, getString(R.string.inicio_desp_action)) {
                    bottomNav.selectedItemId = R.id.nav_ordenes
                }
            }
            Rol.Operador -> {
                tvRolChip.text = getString(R.string.rol_operador)
                tvRolChip.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_op_fg))
                tvRolChip.setBackgroundResource(R.drawable.bg_chip_op)
                tvTitulo.text = getString(R.string.inicio_op_title)
                tvDesc.text = getString(R.string.inicio_op_desc)
                tvExtra.visibility = View.VISIBLE
                tvExtra.text = getString(R.string.inicio_op_watch)
                addAccion(acciones, getString(R.string.inicio_op_action_orden)) {
                    bottomNav.selectedItemId = R.id.nav_ordenes
                }
                addAccion(acciones, getString(R.string.inicio_op_action_cobro))
            }
        }

        contentContainer.addView(view)
    }

    private fun addAccion(
        container: LinearLayout,
        texto: String,
        onClick: (() -> Unit)? = null
    ) {
        val btn = LayoutInflater.from(this)
            .inflate(R.layout.item_accion_inicio, container, false) as TextView
        btn.text = texto
        btn.setOnClickListener {
            if (onClick != null) {
                onClick()
            } else {
                Toast.makeText(this, "$texto (simulado)", Toast.LENGTH_SHORT).show()
            }
        }
        container.addView(btn)
    }

    private fun showStub(title: String, body: String) {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_stub, contentContainer, false)
        view.findViewById<TextView>(R.id.tvStubTitle).text = title
        view.findViewById<TextView>(R.id.tvStubBody).text = body
        contentContainer.addView(view)
    }

    private fun showCuenta() {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_cuenta, contentContainer, false)
        view.findViewById<TextView>(R.id.tvCuentaNombre).text = nombre
        view.findViewById<TextView>(R.id.tvCuentaCorreo).text =
            getString(R.string.cuenta_correo_format, correo)
        view.findViewById<TextView>(R.id.tvCuentaRol).text =
            getString(R.string.cuenta_rol_format, rolLabel())
        view.findViewById<TextView>(R.id.btnCerrarSesion).setOnClickListener {
            MockAuthRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        contentContainer.addView(view)
    }

    private fun rolLabel(): String = when (rol) {
        Rol.Administrador -> getString(R.string.rol_admin)
        Rol.Despachador -> getString(R.string.rol_despachador)
        Rol.Operador -> getString(R.string.rol_operador)
    }
}
