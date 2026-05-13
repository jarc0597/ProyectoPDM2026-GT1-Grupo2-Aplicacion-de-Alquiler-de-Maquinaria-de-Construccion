package com.proyecto.pdm115.alquilermaquinaria

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.proyecto.pdm115.alquilermaquinaria.db.DBHelper
import android.database.sqlite.SQLiteDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var dbLocal: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Abre o crea la base SQLite local
        inicializarBaseDatos()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ir a pantalla de registro
        val btnIrARegistro = findViewById<TextView>(R.id.tv_registro)
        btnIrARegistro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Ir a pantalla principal
        val btnIngreso = findViewById<TextView>(R.id.btn_login)
        btnIngreso.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun inicializarBaseDatos() {
        try {
            // Mantiene la conexión SQLite abierta mientras la pantalla esté activa
            dbHelper = DBHelper(this)
            dbLocal = dbHelper.writableDatabase

            // Verifica las tablas creadas en la base
            val resumen = dbHelper.obtenerResumenBaseDatos()

            Toast.makeText(this, "Base SQLite abierta correctamente", Toast.LENGTH_LONG).show()
            Log.d("BD_SQLITE", resumen)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error SQLite: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            Log.e("BD_SQLITE", "Error al abrir la base de datos", e)
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        // Cierra la base solo cuando se destruye la pantalla
        if (::dbLocal.isInitialized && dbLocal.isOpen) {
            dbLocal.close()
        }
    }
}