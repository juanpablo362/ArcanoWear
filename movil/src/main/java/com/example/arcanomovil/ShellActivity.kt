package com.example.arcanomovil

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.arcanomovil.data.EstadoMesa
import com.example.arcanomovil.data.EstadoOrdenPhone
import com.example.arcanomovil.data.MockAuthRepository
import com.example.arcanomovil.data.MockMesasRepository
import com.example.arcanomovil.data.MockOrdenesRepository
import com.example.arcanomovil.data.MockUsuariosRepository
import com.example.arcanomovil.data.OrdenPhone
import com.example.arcanomovil.data.Rol
import com.example.arcanomovil.data.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView

class ShellActivity : AppCompatActivity() {

    private lateinit var contentContainer: FrameLayout
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var nombre: String
    private lateinit var correo: String
    private lateinit var rol: Rol

    private var editingUserId: String? = null

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

        if (rol == Rol.Administrador) {
            bottomNav.menu.clear()
            bottomNav.inflateMenu(R.menu.menu_bottom_nav_admin)
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    showInicio()
                    true
                }
                R.id.nav_usuarios -> {
                    showUsuarios()
                    true
                }
                R.id.nav_mesas -> {
                    showMesas()
                    true
                }
                R.id.nav_ordenes -> {
                    showStub(getString(R.string.stub_ordenes), textoOrdenesPorRol())
                    true
                }
                R.id.nav_historial -> {
                    if (rol == Rol.Administrador) showHistorial()
                    else showStub(getString(R.string.stub_historial), "Consulta de órdenes procesadas (simulado).")
                    true
                }
                R.id.nav_cuenta -> {
                    showCuenta()
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = if (rol == Rol.Administrador) {
            R.id.nav_usuarios
        } else {
            R.id.nav_inicio
        }
    }

    private fun textoOrdenesPorRol(): String = when (rol) {
        Rol.Administrador -> "Vista general de órdenes del salón."
        Rol.Despachador -> "Aquí gestionarás la cola hasta Listo."
        Rol.Operador -> "Aquí registrarás nuevas órdenes de mesa."
    }

    private fun showInicio() {
        if (rol == Rol.Administrador) {
            showUsuarios()
            return
        }

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
            Rol.Administrador -> Unit
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
            if (onClick != null) onClick()
            else Toast.makeText(this, "$texto (simulado)", Toast.LENGTH_SHORT).show()
        }
        container.addView(btn)
    }

    private fun bindHeader(root: View, title: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
        val header = root.findViewById<View>(R.id.header)
        header.findViewById<TextView>(R.id.tvScreenTitle).text = title
        val action = header.findViewById<TextView>(R.id.btnHeaderAction)
        if (actionText != null && onAction != null) {
            action.visibility = View.VISIBLE
            action.text = actionText
            action.setOnClickListener { onAction() }
        } else {
            action.visibility = View.GONE
        }
    }

    private fun showUsuarios() {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_usuarios, contentContainer, false)
        bindHeader(view, getString(R.string.titulo_usuarios), getString(R.string.btn_nuevo)) {
            showUsuarioForm(null)
        }

        val lista = view.findViewById<LinearLayout>(R.id.listaUsuarios)
        for (usuario in MockUsuariosRepository.todos()) {
            lista.addView(buildUsuarioItem(usuario))
        }
        contentContainer.addView(view)
    }

    private fun buildUsuarioItem(usuario: Usuario): View {
        val item = layoutInflater.inflate(R.layout.item_usuario, contentContainer, false)
        item.findViewById<TextView>(R.id.tvAvatar).text = usuario.iniciales()
        item.findViewById<TextView>(R.id.tvNombre).text = usuario.nombre
        item.findViewById<TextView>(R.id.tvCorreo).text = usuario.correo

        val tvRol = item.findViewById<TextView>(R.id.tvRol)
        when (usuario.rol) {
            Rol.Administrador -> {
                tvRol.text = getString(R.string.rol_admin)
                tvRol.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_admin_fg))
                tvRol.setBackgroundResource(R.drawable.bg_chip_admin)
            }
            Rol.Despachador -> {
                tvRol.text = getString(R.string.rol_despachador)
                tvRol.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_desp_fg))
                tvRol.setBackgroundResource(R.drawable.bg_chip_desp)
            }
            Rol.Operador -> {
                tvRol.text = getString(R.string.rol_operador)
                tvRol.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_op_fg))
                tvRol.setBackgroundResource(R.drawable.bg_chip_op)
            }
        }

        val tvEstado = item.findViewById<TextView>(R.id.tvEstado)
        val dot = item.findViewById<View>(R.id.dotEstado)
        if (usuario.activo) {
            tvEstado.text = getString(R.string.estado_activo)
            tvEstado.setTextColor(ContextCompat.getColor(this, R.color.arcano_status_green))
            dot.setBackgroundResource(R.drawable.bg_dot_green)
        } else {
            tvEstado.text = getString(R.string.estado_inactivo)
            tvEstado.setTextColor(ContextCompat.getColor(this, R.color.arcano_status_inactive))
            dot.setBackgroundResource(R.drawable.bg_dot_red)
        }

        item.findViewById<TextView>(R.id.btnEditar).setOnClickListener {
            showUsuarioForm(usuario.id)
        }
        return item
    }

    private fun showUsuarioForm(userId: String?) {
        editingUserId = userId
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_usuario_form, contentContainer, false)

        val tvTitulo = view.findViewById<TextView>(R.id.tvFormTitulo)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etCorreo = view.findViewById<EditText>(R.id.etCorreo)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val spRol = view.findViewById<Spinner>(R.id.spRol)
        val btnToggle = view.findViewById<TextView>(R.id.btnToggleActivo)
        val btnGuardar = view.findViewById<TextView>(R.id.btnGuardar)
        val btnCancelar = view.findViewById<TextView>(R.id.btnCancelarForm)

        val roles = listOf(
            getString(R.string.rol_admin),
            getString(R.string.rol_despachador),
            getString(R.string.rol_operador)
        )
        spRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        val existente = userId?.let { MockUsuariosRepository.getById(it) }
        if (existente != null) {
            tvTitulo.text = getString(R.string.titulo_editar_usuario)
            etNombre.setText(existente.nombre)
            etCorreo.setText(existente.correo)
            etPassword.setText(existente.password)
            spRol.setSelection(
                when (existente.rol) {
                    Rol.Administrador -> 0
                    Rol.Despachador -> 1
                    Rol.Operador -> 2
                }
            )
            btnToggle.visibility = View.VISIBLE
            btnToggle.text = if (existente.activo) {
                getString(R.string.desactivar)
            } else {
                getString(R.string.activar)
            }
            btnToggle.setOnClickListener {
                MockUsuariosRepository.toggleActivo(existente.id)
                Toast.makeText(this, R.string.usuario_estado_ok, Toast.LENGTH_SHORT).show()
                showUsuarios()
            }
        } else {
            tvTitulo.text = getString(R.string.titulo_nuevo_usuario)
        }

        btnGuardar.setOnClickListener {
            val nombreVal = etNombre.text.toString().trim()
            val correoVal = etCorreo.text.toString().trim()
            val passVal = etPassword.text.toString()
            if (nombreVal.isEmpty() || correoVal.isEmpty() || passVal.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val rolSel = when (spRol.selectedItemPosition) {
                0 -> Rol.Administrador
                1 -> Rol.Despachador
                else -> Rol.Operador
            }
            val usuario = Usuario(
                id = existente?.id ?: MockUsuariosRepository.nextId(),
                nombre = nombreVal,
                correo = correoVal,
                password = passVal,
                rol = rolSel,
                activo = existente?.activo ?: true
            )
            MockUsuariosRepository.guardar(usuario)
            Toast.makeText(this, R.string.usuario_guardado, Toast.LENGTH_SHORT).show()
            showUsuarios()
        }

        btnCancelar.setOnClickListener { showUsuarios() }
        contentContainer.addView(view)
    }

    private fun showMesas() {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_mesas, contentContainer, false)
        bindHeader(view, getString(R.string.titulo_mesas), getString(R.string.btn_nueva_mesa)) {
            MockMesasRepository.agregar()
            Toast.makeText(this, R.string.mesa_agregada, Toast.LENGTH_SHORT).show()
            showMesas()
        }

        val lista = view.findViewById<LinearLayout>(R.id.listaMesas)
        for (mesa in MockMesasRepository.todas()) {
            val item = layoutInflater.inflate(R.layout.item_mesa, lista, false)
            item.findViewById<TextView>(R.id.tvMesa).text =
                getString(R.string.mesa_format, mesa.numero)
            val tvEstado = item.findViewById<TextView>(R.id.tvEstadoMesa)
            when (mesa.estado) {
                EstadoMesa.Disponible -> {
                    tvEstado.text = getString(R.string.mesa_disponible)
                    tvEstado.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_op_fg))
                    tvEstado.setBackgroundResource(R.drawable.bg_chip_op)
                }
                EstadoMesa.Ocupada -> {
                    tvEstado.text = getString(R.string.mesa_ocupada)
                    tvEstado.setTextColor(ContextCompat.getColor(this, R.color.arcano_amber))
                    tvEstado.setBackgroundResource(R.drawable.bg_chip_amber)
                }
                EstadoMesa.Reservada -> {
                    tvEstado.text = getString(R.string.mesa_reservada)
                    tvEstado.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_desp_fg))
                    tvEstado.setBackgroundResource(R.drawable.bg_chip_desp)
                }
            }
            item.setOnClickListener {
                MockMesasRepository.cicloEstado(mesa.id)
                showMesas()
            }
            lista.addView(item)
        }
        contentContainer.addView(view)
    }

    private fun showOrdenes() {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_ordenes, contentContainer, false)
        bindHeader(view, getString(R.string.titulo_ordenes))

        val lista = view.findViewById<LinearLayout>(R.id.listaOrdenes)
        val vacio = view.findViewById<TextView>(R.id.tvVacio)
        val activas = MockOrdenesRepository.activas()
        if (activas.isEmpty()) {
            vacio.visibility = View.VISIBLE
        } else {
            vacio.visibility = View.GONE
            for (orden in activas) {
                lista.addView(buildOrdenItem(orden, lista))
            }
        }
        contentContainer.addView(view)
    }

    private fun showHistorial() {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_historial, contentContainer, false)
        bindHeader(view, getString(R.string.titulo_historial))

        val lista = view.findViewById<LinearLayout>(R.id.listaHistorial)
        val vacio = view.findViewById<TextView>(R.id.tvVacio)
        val etBuscar = view.findViewById<EditText>(R.id.etBuscar)

        fun render(query: String) {
            lista.removeAllViews()
            val items = MockOrdenesRepository.buscarHistorial(query)
            vacio.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            for (orden in items) {
                lista.addView(buildOrdenItem(orden, lista))
            }
        }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                render(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        render("")
        contentContainer.addView(view)
    }

    private fun buildOrdenItem(orden: OrdenPhone, parent: LinearLayout): View {
        val item = layoutInflater.inflate(R.layout.item_orden, parent, false)
        item.findViewById<TextView>(R.id.tvMesa).text = getString(R.string.mesa_format, orden.mesa)
        item.findViewById<TextView>(R.id.tvOrden).text =
            getString(R.string.orden_format, orden.numeroOrden)
        item.findViewById<TextView>(R.id.tvFecha).text = orden.fecha
        item.findViewById<TextView>(R.id.tvTotal).text =
            getString(R.string.precio_format, orden.total)

        val tvEstado = item.findViewById<TextView>(R.id.tvEstado)
        bindEstadoOrden(tvEstado, orden.estado)
        return item
    }

    private fun bindEstadoOrden(tv: TextView, estado: EstadoOrdenPhone) {
        when (estado) {
            EstadoOrdenPhone.Pendiente -> {
                tv.text = getString(R.string.estado_pendiente)
                tv.setTextColor(ContextCompat.getColor(this, R.color.arcano_muted))
                tv.setBackgroundResource(R.drawable.bg_chip_desp)
            }
            EstadoOrdenPhone.EnPreparacion -> {
                tv.text = getString(R.string.estado_en_preparacion)
                tv.setTextColor(ContextCompat.getColor(this, R.color.arcano_amber))
                tv.setBackgroundResource(R.drawable.bg_chip_amber)
            }
            EstadoOrdenPhone.Listo -> {
                tv.text = getString(R.string.estado_listo)
                tv.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_op_fg))
                tv.setBackgroundResource(R.drawable.bg_chip_entregado)
            }
            EstadoOrdenPhone.Recogida -> {
                tv.text = getString(R.string.estado_recogida)
                tv.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_admin_fg))
                tv.setBackgroundResource(R.drawable.bg_chip_admin)
            }
            EstadoOrdenPhone.Entregado -> {
                tv.text = getString(R.string.estado_entregado)
                tv.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_op_fg))
                tv.setBackgroundResource(R.drawable.bg_chip_entregado)
            }
            EstadoOrdenPhone.Pagado -> {
                tv.text = getString(R.string.estado_pagado)
                tv.setTextColor(ContextCompat.getColor(this, R.color.arcano_chip_desp_fg))
                tv.setBackgroundResource(R.drawable.bg_chip_desp)
            }
            EstadoOrdenPhone.Cancelado -> {
                tv.text = getString(R.string.estado_cancelado)
                tv.setTextColor(ContextCompat.getColor(this, R.color.arcano_status_inactive))
                tv.setBackgroundResource(R.drawable.bg_chip_cancelado)
            }
        }
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

        val btnAdminUsuarios = view.findViewById<TextView>(R.id.btnAdminUsuarios)
        btnAdminUsuarios.visibility = View.GONE

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
