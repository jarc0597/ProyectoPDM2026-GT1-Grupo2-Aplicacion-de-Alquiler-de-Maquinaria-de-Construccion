package com.proyecto.pdm115.alquilermaquinaria.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.proyecto.pdm115.alquilermaquinaria.models.Maquinaria
import android.content.ContentValues


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
    fun obtenerMaquinarias(): List<Maquinaria> {
        val listaMaquinaria = mutableListOf<Maquinaria>()
        val db = readableDatabase

        // Consulta maquinaria junto con su categoria y estado
        val cursor = db.rawQuery(
            """
        SELECT 
            m.id_maquinaria,
            m.codigo_interno,
            m.nombre_equipo,
            m.marca,
            m.modelo,
            m.capacidad,
            m.descripcion,
            m.costo_hora,
            m.costo_dia,
            m.stock,
            m.imagen_url,
            m.id_categoria,
            c.nombre_categoria,
            m.id_estado,
            e.nombre_estado,
            m.activo
        FROM maquinaria m
        INNER JOIN categorias_maquinaria c 
            ON c.id_categoria = m.id_categoria
        INNER JOIN estados_maquinaria e 
            ON e.id_estado = m.id_estado
        WHERE m.activo = 1
        ORDER BY m.nombre_equipo ASC
        """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val maquinaria = Maquinaria(
                    idMaquinaria = it.getInt(0),
                    codigoInterno = it.getString(1),
                    nombreEquipo = it.getString(2),
                    marca = it.getString(3),
                    modelo = it.getString(4),
                    capacidad = it.getString(5),
                    descripcion = it.getString(6),
                    costoHora = it.getDouble(7),
                    costoDia = it.getDouble(8),
                    stock = it.getInt(9),
                    imagenUrl = it.getString(10),
                    idCategoria = it.getInt(11),
                    nombreCategoria = it.getString(12),
                    idEstado = it.getInt(13),
                    nombreEstado = it.getString(14),
                    activo = it.getInt(15)
                )

                listaMaquinaria.add(maquinaria)
            }
        }

        return listaMaquinaria
    }
    fun obtenerMaquinariaPorId(idMaquinaria: Int): Maquinaria? {
        val db = readableDatabase

        // Consulta una sola maquinaria usando su ID
        val cursor = db.rawQuery(
            """
        SELECT 
            m.id_maquinaria,
            m.codigo_interno,
            m.nombre_equipo,
            m.marca,
            m.modelo,
            m.capacidad,
            m.descripcion,
            m.costo_hora,
            m.costo_dia,
            m.stock,
            m.imagen_url,
            m.id_categoria,
            c.nombre_categoria,
            m.id_estado,
            e.nombre_estado,
            m.activo
        FROM maquinaria m
        INNER JOIN categorias_maquinaria c 
            ON c.id_categoria = m.id_categoria
        INNER JOIN estados_maquinaria e 
            ON e.id_estado = m.id_estado
        WHERE m.id_maquinaria = ?
        LIMIT 1
        """.trimIndent(),
            arrayOf(idMaquinaria.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                return Maquinaria(
                    idMaquinaria = it.getInt(0),
                    codigoInterno = it.getString(1),
                    nombreEquipo = it.getString(2),
                    marca = it.getString(3),
                    modelo = it.getString(4),
                    capacidad = it.getString(5),
                    descripcion = it.getString(6),
                    costoHora = it.getDouble(7),
                    costoDia = it.getDouble(8),
                    stock = it.getInt(9),
                    imagenUrl = it.getString(10),
                    idCategoria = it.getInt(11),
                    nombreCategoria = it.getString(12),
                    idEstado = it.getInt(13),
                    nombreEstado = it.getString(14),
                    activo = it.getInt(15)
                )
            }
        }

        return null
    }

    fun insertarMaquinaria(
        codigoInterno: String,
        nombreEquipo: String,
        marca: String,
        modelo: String,
        capacidad: String?,
        descripcion: String,
        costoHora: Double,
        costoDia: Double,
        stock: Int,
        imagenUrl: String?,
        idCategoria: Int,
        idEstado: Int
    ): Long {
        val db = writableDatabase

        // ContentValues permite preparar los datos antes de insertarlos en SQLite
        val valores = ContentValues().apply {
            put("codigo_interno", codigoInterno)
            put("nombre_equipo", nombreEquipo)
            put("marca", marca)
            put("modelo", modelo)
            put("capacidad", capacidad)
            put("descripcion", descripcion)
            put("costo_hora", costoHora)
            put("costo_dia", costoDia)
            put("stock", stock)
            put("imagen_url", imagenUrl)
            put("id_categoria", idCategoria)
            put("id_estado", idEstado)
            put("activo", 1)
        }

        // Retorna el ID del nuevo registro o -1 si falló
        return db.insert("maquinaria", null, valores)
    }

    fun actualizarMaquinaria(
        idMaquinaria: Int,
        codigoInterno: String,
        nombreEquipo: String,
        marca: String,
        modelo: String,
        capacidad: String?,
        descripcion: String,
        costoHora: Double,
        costoDia: Double,
        stock: Int,
        imagenUrl: String?,
        idCategoria: Int,
        idEstado: Int
    ): Int {
        val db = writableDatabase

        // Prepara los nuevos valores para actualizar la maquinaria
        val valores = ContentValues().apply {
            put("codigo_interno", codigoInterno)
            put("nombre_equipo", nombreEquipo)
            put("marca", marca)
            put("modelo", modelo)
            put("capacidad", capacidad)
            put("descripcion", descripcion)
            put("costo_hora", costoHora)
            put("costo_dia", costoDia)
            put("stock", stock)
            put("imagen_url", imagenUrl)
            put("id_categoria", idCategoria)
            put("id_estado", idEstado)
        }

        // Retorna cuántos registros fueron actualizados
        return db.update(
            "maquinaria",
            valores,
            "id_maquinaria = ?",
            arrayOf(idMaquinaria.toString())
        )
    }
    fun eliminarMaquinaria(idMaquinaria: Int): Int {
        val db = writableDatabase

        // Eliminación lógica: no borra el registro, solo lo marca como inactivo
        val valores = ContentValues().apply {
            put("activo", 0)
        }

        // Retorna cuántos registros fueron marcados como inactivos
        return db.update(
            "maquinaria",
            valores,
            "id_maquinaria = ?",
            arrayOf(idMaquinaria.toString())
        )
    }

    fun obtenerMaquinariaPorCodigo(codigoInterno: String): Maquinaria? {
        val db = readableDatabase

        // Busca una maquinaria por su código interno
        val cursor = db.rawQuery(
            """
        SELECT 
            m.id_maquinaria,
            m.codigo_interno,
            m.nombre_equipo,
            m.marca,
            m.modelo,
            m.capacidad,
            m.descripcion,
            m.costo_hora,
            m.costo_dia,
            m.stock,
            m.imagen_url,
            m.id_categoria,
            c.nombre_categoria,
            m.id_estado,
            e.nombre_estado,
            m.activo
        FROM maquinaria m
        INNER JOIN categorias_maquinaria c 
            ON c.id_categoria = m.id_categoria
        INNER JOIN estados_maquinaria e 
            ON e.id_estado = m.id_estado
        WHERE m.codigo_interno = ?
        LIMIT 1
        """.trimIndent(),
            arrayOf(codigoInterno)
        )

        cursor.use {
            if (it.moveToFirst()) {
                return Maquinaria(
                    idMaquinaria = it.getInt(0),
                    codigoInterno = it.getString(1),
                    nombreEquipo = it.getString(2),
                    marca = it.getString(3),
                    modelo = it.getString(4),
                    capacidad = it.getString(5),
                    descripcion = it.getString(6),
                    costoHora = it.getDouble(7),
                    costoDia = it.getDouble(8),
                    stock = it.getInt(9),
                    imagenUrl = it.getString(10),
                    idCategoria = it.getInt(11),
                    nombreCategoria = it.getString(12),
                    idEstado = it.getInt(13),
                    nombreEstado = it.getString(14),
                    activo = it.getInt(15)
                )
            }
        }

        return null
    }
}