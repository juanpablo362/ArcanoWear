package com.example.arcanowear

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.arcanowear.data.EstadoOrden
import com.example.arcanowear.data.MockOrdenesRepository

class MainActivity : AppCompatActivity() {

    private lateinit var listaPendientes: LinearLayout
    private lateinit var tvSinPendientes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listaPendientes = findViewById(R.id.listaPendientes)
        tvSinPendientes = findViewById(R.id.tvSinPendientes)
    }

    override fun onResume() {
        super.onResume()
        renderPendientes()
    }

    private fun renderPendientes() {
        listaPendientes.removeAllViews()
        val pendientes = MockOrdenesRepository.pendientes()

        if (pendientes.isEmpty()) {
            tvSinPendientes.visibility = View.VISIBLE
            return
        }

        tvSinPendientes.visibility = View.GONE
        val inflater = LayoutInflater.from(this)

        for (orden in pendientes) {
            val item = inflater.inflate(R.layout.item_orden_pendiente, listaPendientes, false)
            val iconBg = item.findViewById<View>(R.id.iconBg)
            val tvMesa = item.findViewById<TextView>(R.id.tvMesa)
            val tvEstado = item.findViewById<TextView>(R.id.tvEstado)
            val tvChevron = item.findViewById<TextView>(R.id.tvChevron)

            tvMesa.text = getString(R.string.mesa_format, orden.mesa)

            val esRecogida = orden.estado == EstadoOrden.Recogida
            if (esRecogida) {
                item.setBackgroundResource(R.drawable.bg_pill_accent)
                iconBg.setBackgroundResource(R.drawable.bg_circle_icon_red)
                tvEstado.text = getString(R.string.estado_recogida)
                tvEstado.setTextColor(ContextCompat.getColor(this, R.color.arcano_red))
                tvChevron.setTextColor(ContextCompat.getColor(this, R.color.arcano_red))
            } else {
                item.setBackgroundResource(R.drawable.bg_pill)
                iconBg.setBackgroundResource(R.drawable.bg_circle_icon)
                tvEstado.text = getString(R.string.estado_listo)
                tvEstado.setTextColor(ContextCompat.getColor(this, R.color.arcano_gray))
                tvChevron.setTextColor(ContextCompat.getColor(this, R.color.white))
            }

            item.setOnClickListener {
                val destino = if (esRecogida) {
                    Intent(this, EntregaActivity::class.java)
                } else {
                    Intent(this, DetalleActivity::class.java)
                }
                destino.putExtra(OrdenExtras.ORDEN_ID, orden.id)
                startActivity(destino)
            }

            listaPendientes.addView(item)
        }
    }
}
