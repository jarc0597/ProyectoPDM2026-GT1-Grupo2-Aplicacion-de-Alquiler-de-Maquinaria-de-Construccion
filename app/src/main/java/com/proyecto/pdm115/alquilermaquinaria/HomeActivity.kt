package com.proyecto.pdm115.alquilermaquinaria

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.pdm115.alquilermaquinaria.adapters.MaquinariaAdapter
import com.proyecto.pdm115.alquilermaquinaria.db.DBHelper

class HomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var rvMaquinaria: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa la conexión con SQLite
        dbHelper = DBHelper(this)

        // Carga las maquinarias en pantalla
        cargarCatalogoMaquinaria()

        val btn = findViewById<TextView>(R.id.tv_tag_oferta)

        btn.setOnClickListener {
            val intent = Intent(this, activity_detalle_maquinaria::class.java)
            startActivity(intent)
        }

        // Acceso temporal al módulo administrador de maquinaria
        val btnAdmin = findViewById<ImageButton>(R.id.btn_notifications)

        btnAdmin.setOnClickListener {
            val intent = Intent(this, GestionMaquinariaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Recarga el catálogo al regresar desde gestión de maquinaria
        if (::dbHelper.isInitialized) {
            cargarCatalogoMaquinaria()
        }
    }

    private fun cargarCatalogoMaquinaria() {
        // Busca el RecyclerView en activity_home.xml
        rvMaquinaria = findViewById(R.id.rv_maquinaria_disp)

        // Obtiene las maquinarias guardadas en SQLite
        val listaMaquinaria = dbHelper.obtenerMaquinarias()

        // Mensaje para confirmar en Logcat cuántas cargó
        Log.d("BD_SQLITE", "HomeActivity maquinaria cargada: ${listaMaquinaria.size}")

        // Muestra las tarjetas en forma horizontal
        rvMaquinaria.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // Conecta la lista con el adaptador visual
        rvMaquinaria.adapter = MaquinariaAdapter(listaMaquinaria)
    }
}