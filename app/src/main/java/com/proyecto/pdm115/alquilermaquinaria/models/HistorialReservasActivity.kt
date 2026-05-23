package com.proyecto.pdm115.alquilermaquinaria

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.proyecto.pdm115.alquilermaquinaria.db.DBHelper

class HistorialReservasActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var contenedorHistorial: LinearLayout
    private lateinit var btnVolverHistorial: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga la pantalla de historial
        setContentView(R.layout.activity_historial_reservas)

        // Inicializa SQLite
        dbHelper = DBHelper(this)

        // Enlaza componentes visuales
        contenedorHistorial = findViewById(R.id.contenedor_historial_reservas)
        btnVolverHistorial = findViewById(R.id.btn_volver_historial)

        // Carga reservas guardadas
        cargarHistorial()

        btnVolverHistorial.setOnClickListener {
            finish()
        }
    }

    private fun cargarHistorial() {
        val historial: List<String> = dbHelper.obtenerHistorialReservas()

        contenedorHistorial.removeAllViews()

        if (historial.isEmpty()) {
            val tvVacio = TextView(this).apply {
                text = "No hay reservas registradas."
                textSize = 16f
                setTextColor(android.graphics.Color.parseColor("#64748B"))
                setPadding(0, 20, 0, 20)
            }

            contenedorHistorial.addView(tvVacio)
            return
        }

        for (reserva in historial) {
            val tvReserva = TextView(this).apply {
                text = reserva
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#0B1C30"))
                setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
                setPadding(24, 24, 24, 24)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(0, 0, 0, 20)
            tvReserva.layoutParams = params

            contenedorHistorial.addView(tvReserva)
        }
    }
}