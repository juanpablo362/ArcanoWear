package com.example.arcanowear

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arcanowear.data.MockOrdenesRepository

class DetalleActivity : AppCompatActivity() {

    private var ordenId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle)

        ordenId = intent.getStringExtra(OrdenExtras.ORDEN_ID).orEmpty()
        val orden = MockOrdenesRepository.getById(ordenId)
        if (orden == null) {
            finish()
            return
        }

        val tvMesa = findViewById<TextView>(R.id.tvMesa)
        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        val listaProductos = findViewById<LinearLayout>(R.id.listaProductos)
        val btnConfirmar = findViewById<TextView>(R.id.btnConfirmarRecoleccion)

        tvMesa.text = getString(R.string.mesa_format, orden.mesa)
        tvEstado.text = getString(R.string.estado_listo)

        val inflater = LayoutInflater.from(this)
        for (producto in orden.productos) {
            val fila = inflater.inflate(R.layout.item_producto, listaProductos, false)
            val tvProducto = fila.findViewById<TextView>(R.id.tvProducto)
            tvProducto.text = getString(R.string.producto_format, producto.cantidad, producto.nombre)
            listaProductos.addView(fila)
        }

        btnConfirmar.setOnClickListener {
            val ok = MockOrdenesRepository.confirmarRecoleccion(ordenId)
            if (ok) {
                Toast.makeText(this, R.string.estado_recogida, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, EntregaActivity::class.java)
                intent.putExtra(OrdenExtras.ORDEN_ID, ordenId)
                startActivity(intent)
                finish()
            }
        }
    }
}
