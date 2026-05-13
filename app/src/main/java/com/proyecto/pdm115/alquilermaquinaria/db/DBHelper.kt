package com.proyecto.pdm115.alquilermaquinaria.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    private val appContext: Context = context.applicationContext

    companion object {
        private const val DATABASE_NAME = "alquiler_maquinaria.db"

        // Cambiar versión obliga a reconstruir la base en el emulador
        private const val DATABASE_VERSION = 7

        private const val SCRIPT_SCHEMA = "sql/02_sqlite_schema.sql"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)

        // Activa llaves foráneas en SQLite
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        ejecutarScriptDesdeAssets(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        ejecutarScriptDesdeAssets(db)
    }

    private fun ejecutarScriptDesdeAssets(db: SQLiteDatabase) {
        val scriptSql = appContext.assets.open(SCRIPT_SCHEMA)
            .bufferedReader()
            .use { it.readText() }

        val sentencias = dividirScriptSql(scriptSql)

        db.beginTransaction()
        try {
            for (sentencia in sentencias) {
                val sql = sentencia.trim()

                // PRAGMA se maneja desde onConfigure
                if (sql.isNotEmpty() && !sql.startsWith("PRAGMA", ignoreCase = true)) {
                    db.execSQL(sql)
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun dividirScriptSql(script: String): List<String> {
        val sentencias = mutableListOf<String>()
        val acumulador = StringBuilder()
        var dentroTrigger = false

        for (lineaOriginal in script.lines()) {
            val lineaLimpia = lineaOriginal.trim()

            // Ignora comentarios y líneas vacías
            if (lineaLimpia.isEmpty() || lineaLimpia.startsWith("--")) {
                continue
            }

            // Detecta inicio de trigger
            if (lineaLimpia.startsWith("CREATE TRIGGER", ignoreCase = true)) {
                dentroTrigger = true
            }

            acumulador.appendLine(lineaOriginal)

            if (dentroTrigger) {
                // El END final del trigger viene sin espacios al inicio
                if (lineaOriginal.startsWith("END;", ignoreCase = true)) {
                    sentencias.add(acumulador.toString().trim())
                    acumulador.clear()
                    dentroTrigger = false
                }
            } else {
                // Sentencias normales: CREATE TABLE, INDEX, DROP, INSERT, etc.
                if (lineaLimpia.endsWith(";")) {
                    sentencias.add(acumulador.toString().trim())
                    acumulador.clear()
                }
            }
        }

        val restante = acumulador.toString().trim()
        if (restante.isNotEmpty()) {
            sentencias.add(restante)
        }

        return sentencias
    }

    fun obtenerResumenBaseDatos(): String {
        val db = readableDatabase
        val tablas = mutableListOf<String>()

        // Lista tablas creadas por el script
        val cursor = db.rawQuery(
            """
            SELECT name
            FROM sqlite_master
            WHERE type = 'table'
            AND name NOT LIKE 'sqlite_%'
            AND name NOT LIKE 'android_%'
            ORDER BY name
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                tablas.add(it.getString(0))
            }
        }

        return if (tablas.isEmpty()) {
            "Base creada, pero no se encontraron tablas."
        } else {
            "Tablas encontradas (${tablas.size}): ${tablas.joinToString(", ")}"
        }
    }
}