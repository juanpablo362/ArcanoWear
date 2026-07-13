package com.example.arcanowear

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arcanowear.data.MockOrdenesRepository

class EntregaActivity : AppCompatActivity() {

    private var ordenId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrega)

        ordenId = intent.getStringExtra(OrdenExtras.ORDEN_ID).orEmpty()
        val orden = MockOrdenesRepository.getById(ordenId)
        if (orden == null) {
            finish()
            return
        }

        val tvMesa = findViewById<TextView>(R.id.tvMesa)
        val btnConfirmar = findViewById<LinearLayout>(R.id.btnConfirmarEntrega)
        val btnCancelar = findViewById<TextView>(R.id.btnCancelar)

        tvMesa.text = getString(R.string.mesa_format, orden.mesa)

        btnConfirmar.setOnClickListener {
            val ok = MockOrdenesRepository.confirmarEntrega(ordenId)
            if (ok) {
                Toast.makeText(this, R.string.confirmar_entrega, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}
