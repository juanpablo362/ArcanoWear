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
import com.example.arcanomovil.data.LineaOrden
import com.example.arcanomovil.data.MetodoPago
import com.example.arcanomovil.data.MockMesasRepository
import com.example.arcanomovil.data.MockOrdenesRepository
import com.example.arcanomovil.data.MockProductosRepository
import com.example.arcanomovil.data.MockUsuariosRepository
import com.example.arcanomovil.data.OrdenPhone
import com.example.arcanomovil.data.ProductoMenu
import com.example.arcanomovil.data.Rol
import com.example.arcanomovil.data.SessionRepository
import com.example.arcanomovil.data.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView

class ShellActivity : AppCompatActivity() {

    private lateinit var contentContainer: FrameLayout
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var nombre: String
    private lateinit var correo: String
    private lateinit var rol: Rol

    private var editingUserId: String? = null
    private var filtroGestion: EstadoOrdenPhone? = null
    private var mesaSeleccionadaOrden: Int? = null
    private val cantidadesMenu = mutableMapOf<String, Int>()
    private var mesaCobro: Int? = null
    private var metodoPago: MetodoPago = MetodoPago.Tarjeta

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

        when (rol) {
            Rol.Administrador -> {
                bottomNav.menu.clear()
                bottomNav.inflateMenu(R.menu.menu_bottom_nav_admin)
            }
            Rol.Operador -> {
                bottomNav.menu.clear()
                bottomNav.inflateMenu(R.menu.menu_bottom_nav_operador)
            }
            Rol.Despachador -> {
                bottomNav.menu.clear()
                bottomNav.inflateMenu(R.menu.menu_bottom_nav_despachador)
            }
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
                    showOrdenesPorRol()
                    true
                }
                R.id.nav_cobrar -> {
                    showCierreMesa()
                    true
                }
                R.id.nav_historial -> {
                    showHistorial()
                    true
                }
                R.id.nav_cuenta -> {
                    showCuenta()
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = when (rol) {
            Rol.Administrador -> R.id.nav_usuarios
            Rol.Operador, Rol.Despachador -> R.id.nav_ordenes
        }
    }

    private fun showInicio() {
        // Ya no hay home genérico: cada rol entra a su pantalla operativa.
        when (rol) {
            Rol.Administrador -> showUsuarios()
            Rol.Despachador -> showGestionOrdenes()
            Rol.Operador -> showNuevaOrden()
        }
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

    private fun showOrdenesPorRol() {
        when (rol) {
            Rol.Operador -> showNuevaOrden()
            Rol.Despachador -> showGestionOrdenes()
            Rol.Administrador -> showHistorial()
        }
    }

    private fun showNuevaOrden() {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_nueva_orden, contentContainer, false)
        val chips = view.findViewById<LinearLayout>(R.id.listaMesasChips)
        val tvSinMesas = view.findViewById<TextView>(R.id.tvSinMesas)
        val listaProductos = view.findViewById<LinearLayout>(R.id.listaProductosMenu)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalOrden)
        val btnRegistrar = view.findViewById<TextView>(R.id.btnRegistrarOrden)

        val disponibles = MockMesasRepository.disponibles()
        if (disponibles.isEmpty()) {
            tvSinMesas.visibility = View.VISIBLE
            mesaSeleccionadaOrden = null
        } else {
            tvSinMesas.visibility = View.GONE
            if (mesaSeleccionadaOrden == null || disponibles.none { it.numero == mesaSeleccionadaOrden }) {
                mesaSeleccionadaOrden = disponibles.first().numero
            }
        }

        fun pintarChips() {
            chips.removeAllViews()
            for (mesa in disponibles) {
                val chip = layoutInflater.inflate(R.layout.item_mesa_chip, chips, false) as TextView
                chip.text = getString(R.string.mesa_format, mesa.numero)
                val selected = mesa.numero == mesaSeleccionadaOrden
                chip.setBackgroundResource(
                    if (selected) R.drawable.bg_chip_mesa_selected else R.drawable.bg_chip_mesa
                )
                chip.setTextColor(
                    ContextCompat.getColor(
                        this,
                        if (selected) R.color.arcano_red else R.color.arcano_text
                    )
                )
                chip.setOnClickListener {
                    mesaSeleccionadaOrden = mesa.numero
                    pintarChips()
                }
                chips.addView(chip)
            }
        }
        pintarChips()

        fun actualizarTotal() {
            val total = MockProductosRepository.menu().sumOf { p ->
                (cantidadesMenu[p.id] ?: 0) * p.precio
            }
            tvTotal.text = getString(R.string.total_valor, getString(R.string.precio_format, total))
        }

        fun renderProductos() {
            listaProductos.removeAllViews()
            for (producto in MockProductosRepository.menu()) {
                listaProductos.addView(buildProductoMenuItem(producto, listaProductos) { actualizarTotal() })
            }
            actualizarTotal()
        }
        renderProductos()

        btnRegistrar.setOnClickListener {
            val mesa = mesaSeleccionadaOrden
            val lineas = MockProductosRepository.menu().mapNotNull { p ->
                val qty = cantidadesMenu[p.id] ?: 0
                if (qty > 0) LineaOrden(qty, p.nombre, p.precio) else null
            }
            if (mesa == null || lineas.isEmpty()) {
                Toast.makeText(this, R.string.elige_mesa_productos, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MockOrdenesRepository.registrar(mesa, 2, lineas)
            cantidadesMenu.clear()
            mesaSeleccionadaOrden = null
            Toast.makeText(this, R.string.orden_registrada, Toast.LENGTH_SHORT).show()
            showNuevaOrden()
        }

        contentContainer.addView(view)
    }

    private fun buildProductoMenuItem(
        producto: ProductoMenu,
        parent: LinearLayout,
        onChange: () -> Unit
    ): View {
        val item = layoutInflater.inflate(R.layout.item_producto_menu, parent, false)
        item.findViewById<TextView>(R.id.tvNombreProducto).text = producto.nombre
        item.findViewById<TextView>(R.id.tvPrecioProducto).text =
            getString(R.string.precio_format, producto.precio)
        val tvCantidad = item.findViewById<TextView>(R.id.tvCantidad)
        fun sync() {
            tvCantidad.text = (cantidadesMenu[producto.id] ?: 0).toString()
        }
        sync()
        item.findViewById<TextView>(R.id.btnMas).setOnClickListener {
            cantidadesMenu[producto.id] = (cantidadesMenu[producto.id] ?: 0) + 1
            sync()
            onChange()
        }
        item.findViewById<TextView>(R.id.btnMenos).setOnClickListener {
            val actual = cantidadesMenu[producto.id] ?: 0
            cantidadesMenu[producto.id] = (actual - 1).coerceAtLeast(0)
            sync()
            onChange()
        }
        return item
    }

    private fun showGestionOrdenes() {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_gestion_ordenes, contentContainer, false)
        val filtros = view.findViewById<LinearLayout>(R.id.filtrosEstado)
        val lista = view.findViewById<LinearLayout>(R.id.listaGestion)
        val vacio = view.findViewById<TextView>(R.id.tvVacioGestion)

        data class FiltroUi(val label: String, val estado: EstadoOrdenPhone?)
        val opciones = listOf(
            FiltroUi(getString(R.string.filtro_todas), null),
            FiltroUi(getString(R.string.estado_pendiente), EstadoOrdenPhone.Pendiente),
            FiltroUi(getString(R.string.estado_en_preparacion), EstadoOrdenPhone.EnPreparacion),
            FiltroUi(getString(R.string.estado_listo), EstadoOrdenPhone.Listo)
        )

        fun pintarFiltros() {
            filtros.removeAllViews()
            for (op in opciones) {
                val chip = layoutInflater.inflate(R.layout.item_filtro_chip, filtros, false) as TextView
                chip.text = op.label
                val active = filtroGestion == op.estado
                chip.setBackgroundResource(
                    if (active) R.drawable.bg_filter_active else R.drawable.bg_filter_inactive
                )
                chip.setTextColor(
                    ContextCompat.getColor(
                        this,
                        if (active) R.color.white else R.color.arcano_muted
                    )
                )
                chip.setOnClickListener {
                    filtroGestion = op.estado
                    showGestionOrdenes()
                }
                filtros.addView(chip)
            }
        }

        fun pintarLista() {
            lista.removeAllViews()
            val items = MockOrdenesRepository.gestionDespacho(filtroGestion)
            vacio.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            for (orden in items) {
                lista.addView(buildGestionItem(orden, lista))
            }
        }

        pintarFiltros()
        pintarLista()
        contentContainer.addView(view)
    }

    private fun buildGestionItem(orden: OrdenPhone, parent: LinearLayout): View {
        val item = layoutInflater.inflate(R.layout.item_orden_gestion, parent, false)
        item.findViewById<TextView>(R.id.tvMesaNum).text = orden.mesa.toString()
        item.findViewById<TextView>(R.id.tvMesaTitulo).text =
            getString(R.string.mesa_format, orden.mesa)
        item.findViewById<TextView>(R.id.tvPersonas).text =
            getString(R.string.personas_format, orden.personas)
        item.findViewById<TextView>(R.id.tvHora).text = orden.hora
        item.findViewById<TextView>(R.id.tvProductos).text =
            orden.productos.joinToString("\n") {
                getString(R.string.linea_producto_format, it.cantidad, it.nombre)
            }

        val tvEstado = item.findViewById<TextView>(R.id.tvEstado)
        bindEstadoOrden(tvEstado, orden.estado)

        val btnAvanzar = item.findViewById<TextView>(R.id.btnAvanzar)
        val puedeAvanzar = orden.estado == EstadoOrdenPhone.Pendiente ||
            orden.estado == EstadoOrdenPhone.EnPreparacion
        btnAvanzar.visibility = if (puedeAvanzar) View.VISIBLE else View.GONE
        btnAvanzar.setOnClickListener {
            if (MockOrdenesRepository.avanceEstado(orden.id)) {
                Toast.makeText(this, R.string.estado_avanzado, Toast.LENGTH_SHORT).show()
                showGestionOrdenes()
            }
        }
        return item
    }

    private fun showCierreMesa() {
        contentContainer.removeAllViews()
        val view = layoutInflater.inflate(R.layout.content_cierre_mesa, contentContainer, false)
        val chips = view.findViewById<LinearLayout>(R.id.listaMesasCobro)
        val tvSin = view.findViewById<TextView>(R.id.tvSinCuentas)
        val bloque = view.findViewById<LinearLayout>(R.id.bloqueResumen)
        val tvMesaGrande = view.findViewById<TextView>(R.id.tvMesaGrande)
        val listaLineas = view.findViewById<LinearLayout>(R.id.listaLineasCuenta)
        val tvSubtotal = view.findViewById<TextView>(R.id.tvSubtotal)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalCuenta)
        val btnEfectivo = view.findViewById<TextView>(R.id.btnEfectivo)
        val btnTarjeta = view.findViewById<TextView>(R.id.btnTarjeta)
        val btnConfirmar = view.findViewById<TextView>(R.id.btnConfirmarPago)

        val mesas = MockOrdenesRepository.mesasConCuentaPendiente()
        if (mesas.isEmpty()) {
            tvSin.visibility = View.VISIBLE
            bloque.visibility = View.GONE
            mesaCobro = null
        } else {
            tvSin.visibility = View.GONE
            if (mesaCobro == null || mesaCobro !in mesas) {
                mesaCobro = mesas.first()
            }
        }

        fun pintarMetodo() {
            val efectivo = metodoPago == MetodoPago.Efectivo
            btnEfectivo.setBackgroundResource(
                if (efectivo) R.drawable.bg_chip_mesa_selected else R.drawable.bg_chip_mesa
            )
            btnTarjeta.setBackgroundResource(
                if (!efectivo) R.drawable.bg_chip_mesa_selected else R.drawable.bg_chip_mesa
            )
            btnEfectivo.setTextColor(
                ContextCompat.getColor(this, if (efectivo) R.color.arcano_text else R.color.arcano_muted)
            )
            btnTarjeta.setTextColor(
                ContextCompat.getColor(this, if (!efectivo) R.color.arcano_text else R.color.arcano_muted)
            )
        }

        fun pintarResumen() {
            val mesa = mesaCobro ?: return
            bloque.visibility = View.VISIBLE
            tvMesaGrande.text = mesa.toString()
            listaLineas.removeAllViews()
            val ordenes = MockOrdenesRepository.cuentaMesa(mesa)
            val lineas = ordenes.flatMap { it.productos }
            for (linea in lineas) {
                val row = layoutInflater.inflate(R.layout.item_linea_cuenta, listaLineas, false)
                row.findViewById<TextView>(R.id.tvLinea).text =
                    getString(R.string.linea_producto_format, linea.cantidad, linea.nombre)
                row.findViewById<TextView>(R.id.tvLineaPrecio).text =
                    getString(R.string.precio_format, linea.subtotal())
                listaLineas.addView(row)
            }
            val total = lineas.sumOf { it.subtotal() }
            tvSubtotal.text = getString(R.string.precio_format, total)
            tvTotal.text = getString(R.string.precio_format, total)
        }

        fun pintarChips() {
            chips.removeAllViews()
            for (mesa in mesas) {
                val chip = layoutInflater.inflate(R.layout.item_mesa_chip, chips, false) as TextView
                chip.text = getString(R.string.mesa_format, mesa)
                val selected = mesa == mesaCobro
                chip.setBackgroundResource(
                    if (selected) R.drawable.bg_chip_mesa_selected else R.drawable.bg_chip_mesa
                )
                chip.setTextColor(
                    ContextCompat.getColor(
                        this,
                        if (selected) R.color.arcano_red else R.color.arcano_text
                    )
                )
                chip.setOnClickListener {
                    mesaCobro = mesa
                    pintarChips()
                    pintarResumen()
                }
                chips.addView(chip)
            }
        }

        btnEfectivo.setOnClickListener {
            metodoPago = MetodoPago.Efectivo
            pintarMetodo()
        }
        btnTarjeta.setOnClickListener {
            metodoPago = MetodoPago.Tarjeta
            pintarMetodo()
        }
        btnConfirmar.setOnClickListener {
            val mesa = mesaCobro
            if (mesa == null) {
                Toast.makeText(this, R.string.elige_mesa_cobro, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (MockOrdenesRepository.confirmarPagoMesa(mesa, metodoPago)) {
                Toast.makeText(this, R.string.pago_ok, Toast.LENGTH_SHORT).show()
                mesaCobro = null
                showCierreMesa()
            }
        }

        pintarChips()
        if (mesaCobro != null) {
            pintarResumen()
            pintarMetodo()
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
            SessionRepository.logout()
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
