package com.proyecto.pdm115.alquilermaquinaria

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.widget.Toast
import com.proyecto.pdm115.alquilermaquinaria.db.DBHelper
import android.widget.ImageView
import android.widget.TextView

class activity_detalle_maquinaria : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_maquinaria)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Recibe el ID enviado desde la tarjeta de maquinaria
        val idMaquinaria = intent.getIntExtra("id_maquinaria", -1)

        // Carga los datos reales desde SQLite
        cargarDetalleMaquinaria(idMaquinaria)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // Esto cierra la pantalla actual y vuelve a la anterior
        }
    }

    private fun cargarDetalleMaquinaria(idMaquinaria: Int) {
        if (idMaquinaria == -1) {
            Toast.makeText(this, "No se recibió la maquinaria seleccionada", Toast.LENGTH_LONG).show()
            Log.e("BD_SQLITE", "No se recibió id_maquinaria")
            return
        }

        // Consulta la maquinaria seleccionada en SQLite
        val dbHelper = DBHelper(this)
        val maquinaria = dbHelper.obtenerMaquinariaPorId(idMaquinaria)

        if (maquinaria == null) {
            Toast.makeText(this, "No se encontró la maquinaria", Toast.LENGTH_LONG).show()
            Log.e("BD_SQLITE", "No se encontró maquinaria con ID: $idMaquinaria")
            return
        }

        // Referencias a los elementos visuales del XML
        val imgMaquinaria = findViewById<ImageView>(R.id.img_maquinaria_grande)
        val tvNombre = findViewById<TextView>(R.id.tv_detalle_nombre)
        val tvPrecioDiario = findViewById<TextView>(R.id.tv_precio_diario)
        val tvPrecioHora = findViewById<TextView>(R.id.tv_precio_hora)
        val tvDescripcion = findViewById<TextView>(R.id.tv_detalle_descripcion)
        val tvTotalEstimado = findViewById<TextView>(R.id.tv_total_estimado)

        // Carga datos reales de SQLite en pantalla
        tvNombre.text = maquinaria.nombreEquipo
        tvPrecioDiario.text = "$${String.format("%.2f", maquinaria.costoDia)} /día"
        tvPrecioHora.text = "$${String.format("%.2f", maquinaria.costoHora)} /hr"
        tvTotalEstimado.text = "$${String.format("%.2f", maquinaria.costoDia)} /día"

        tvDescripcion.text = """
        ${maquinaria.descripcion}

        Marca: ${maquinaria.marca}
        Modelo: ${maquinaria.modelo}
        Capacidad: ${maquinaria.capacidad ?: "No especificada"}
        Categoría: ${maquinaria.nombreCategoria ?: "Sin categoría"}
        Estado: ${maquinaria.nombreEstado ?: "Sin estado"}
        Stock disponible: ${maquinaria.stock}
    """.trimIndent()

        // Imagen temporal mientras se integran imágenes reales por maquinaria
        imgMaquinaria.setImageResource(R.drawable.backgrounf_hero)

        // Confirmación técnica en Logcat
        Log.d("BD_SQLITE", "Detalle recibido ID: ${maquinaria.idMaquinaria}")
        Log.d("BD_SQLITE", "Nombre: ${maquinaria.nombreEquipo}")
        Log.d("BD_SQLITE", "Marca: ${maquinaria.marca}")
        Log.d("BD_SQLITE", "Modelo: ${maquinaria.modelo}")
    }
}

