package com.proyecto.pdm115.alquilermaquinaria

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.proyecto.pdm115.alquilermaquinaria.db.DBHelper
import android.content.Intent

class ReservaMaquinariaActivity : AppCompatActivity() {

    private var idMaquinaria: Int = -1
    private var nombreMaquinaria: String = ""
    private var costoDia: Double = 0.0
    private var costoEstimado: Double = 0.0

    private lateinit var tvReservaMaquinaria: TextView
    private lateinit var etFechaInicio: EditText
    private lateinit var etFechaFin: EditText
    private lateinit var etDireccionUso: EditText
    private lateinit var etObservaciones: EditText
    private lateinit var etCantidad: EditText
    private lateinit var tvCostoEstimado: TextView
    private lateinit var btnCalcularReserva: Button
    private lateinit var btnGuardarReserva: Button
    private lateinit var btnVolverReserva: Button

    private lateinit var dbHelper: DBHelper

    private lateinit var btnVerHistorial: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga la pantalla para reservar maquinaria
        setContentView(R.layout.activity_reserva_maquinaria)

        // Inicializa la conexión con SQLite
        dbHelper = DBHelper(this)

        // Recibe los datos enviados desde el detalle
        recibirDatosMaquinaria()

        // Enlaza los componentes visuales
        inicializarVistas()

        // Muestra la maquinaria seleccionada
        tvReservaMaquinaria.text = "Maquinaria: $nombreMaquinaria"

        // Configura botones
        configurarEventos()
    }

    private fun recibirDatosMaquinaria() {
        idMaquinaria = intent.getIntExtra("id_maquinaria", -1)
        nombreMaquinaria = intent.getStringExtra("nombre_maquinaria") ?: "Sin maquinaria"
        costoDia = intent.getDoubleExtra("costo_dia", 0.0)
    }

    private fun inicializarVistas() {
        tvReservaMaquinaria = findViewById(R.id.tv_reserva_maquinaria)
        etFechaInicio = findViewById(R.id.et_fecha_inicio)
        etFechaFin = findViewById(R.id.et_fecha_fin)
        etDireccionUso = findViewById(R.id.et_direccion_uso)
        etObservaciones = findViewById(R.id.et_observaciones)
        etCantidad = findViewById(R.id.et_cantidad)
        tvCostoEstimado = findViewById(R.id.tv_costo_estimado)
        btnCalcularReserva = findViewById(R.id.btn_calcular_reserva)
        btnGuardarReserva = findViewById(R.id.btn_guardar_reserva)
        btnVerHistorial = findViewById(R.id.btn_ver_historial)
        btnVolverReserva = findViewById(R.id.btn_volver_reserva)
    }

    private fun configurarEventos() {
        btnCalcularReserva.setOnClickListener {
            calcularCostoReserva()
        }

        btnGuardarReserva.setOnClickListener {
            guardarReserva()

        }
        btnVerHistorial.setOnClickListener {
            val intent = Intent(this, HistorialReservasActivity::class.java)
            startActivity(intent)
        }

        btnVolverReserva.setOnClickListener {
            finish()
        }
    }

    private fun calcularCostoReserva() {
        val fechaInicio = etFechaInicio.text.toString().trim()
        val fechaFin = etFechaFin.text.toString().trim()
        val cantidadTexto = etCantidad.text.toString().trim()

        if (idMaquinaria == -1) {
            Toast.makeText(this, "No se recibió la maquinaria", Toast.LENGTH_LONG).show()
            return
        }

        if (fechaInicio.isEmpty()) {
            etFechaInicio.error = "Ingrese la fecha de inicio"
            return
        }

        if (fechaFin.isEmpty()) {
            etFechaFin.error = "Ingrese la fecha de fin"
            return
        }

        if (cantidadTexto.isEmpty()) {
            etCantidad.error = "Ingrese la cantidad"
            return
        }

        val cantidad = cantidadTexto.toInt()

        val dias = calcularDias(fechaInicio, fechaFin)

        if (dias <= 0) {
            Toast.makeText(
                this,
                "La fecha fin debe ser igual o posterior a la fecha inicio",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        costoEstimado = dias * costoDia * cantidad

        tvCostoEstimado.text = "Costo estimado: $${String.format("%.2f", costoEstimado)}"
    }

    private fun calcularDias(fechaInicio: String, fechaFin: String): Long {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            val inicio = formato.parse(fechaInicio)
            val fin = formato.parse(fechaFin)

            if (inicio == null || fin == null) {
                return 0
            }

            val diferencia = fin.time - inicio.time

            // Se suma 1 para contar también el día de inicio
            TimeUnit.MILLISECONDS.toDays(diferencia) + 1

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Formato de fecha inválido. Use YYYY-MM-DD",
                Toast.LENGTH_LONG
            ).show()
            0
        }
    }

    private fun guardarReserva() {
        val fechaInicio = etFechaInicio.text.toString().trim()
        val fechaFin = etFechaFin.text.toString().trim()
        val direccionUso = etDireccionUso.text.toString().trim()
        val observaciones = etObservaciones.text.toString().trim()
        val cantidadTexto = etCantidad.text.toString().trim()

        if (idMaquinaria == -1) {
            Toast.makeText(this, "No se recibió la maquinaria seleccionada", Toast.LENGTH_LONG).show()
            return
        }

        if (fechaInicio.isEmpty()) {
            etFechaInicio.error = "Ingrese la fecha de inicio"
            return
        }

        if (fechaFin.isEmpty()) {
            etFechaFin.error = "Ingrese la fecha de fin"
            return
        }

        if (direccionUso.isEmpty()) {
            etDireccionUso.error = "Ingrese la dirección de uso"
            return
        }

        if (cantidadTexto.isEmpty()) {
            etCantidad.error = "Ingrese la cantidad"
            return
        }

        val cantidad = cantidadTexto.toInt()
        val dias = calcularDias(fechaInicio, fechaFin)

        if (dias <= 0) {
            Toast.makeText(this, "Rango de fechas inválido", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val idReserva = dbHelper.guardarReservaMaquinaria(
                idMaquinaria = idMaquinaria,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                direccionUso = direccionUso,
                observaciones = observaciones,
                cantidadEquipos = cantidad,
                costoDia = costoDia,
                diasReserva = dias
            )

            Toast.makeText(
                this,
                "Reserva guardada correctamente. ID: $idReserva",
                Toast.LENGTH_LONG
            ).show()

            finish()

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo guardar la reserva: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}