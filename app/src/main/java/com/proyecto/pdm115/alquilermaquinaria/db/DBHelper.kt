package com.proyecto.pdm115.alquilermaquinaria.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "alquiler_maquinaria.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE roles (
                id_rol INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_rol TEXT NOT NULL,
                descripcion TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE opciones_menu (
                id_opcion INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_opcion TEXT NOT NULL,
                descripcion TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE roles_opciones_menu (
                id_rol INTEGER NOT NULL,
                id_opcion INTEGER NOT NULL,
                PRIMARY KEY (id_rol, id_opcion),
                FOREIGN KEY (id_rol) REFERENCES roles(id_rol),
                FOREIGN KEY (id_opcion) REFERENCES opciones_menu(id_opcion)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE usuarios (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                correo TEXT NOT NULL UNIQUE,
                telefono TEXT,
                contrasena TEXT NOT NULL,
                id_rol INTEGER NOT NULL,
                FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE categorias (
                id_categoria INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_categoria TEXT NOT NULL,
                descripcion TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE maquinaria (
                id_maquinaria INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                marca TEXT NOT NULL,
                modelo TEXT NOT NULL,
                capacidad TEXT,
                descripcion TEXT,
                costo_por_dia REAL NOT NULL,
                estado TEXT NOT NULL,
                imagen TEXT,
                id_categoria INTEGER NOT NULL,
                FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE reservas (
                id_reserva INTEGER PRIMARY KEY AUTOINCREMENT,
                id_usuario INTEGER NOT NULL,
                id_maquinaria INTEGER NOT NULL,
                fecha_inicio TEXT NOT NULL,
                fecha_fin TEXT NOT NULL,
                lugar_uso TEXT NOT NULL,
                costo_total REAL NOT NULL,
                estado_reserva TEXT NOT NULL,
                FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
                FOREIGN KEY (id_maquinaria) REFERENCES maquinaria(id_maquinaria)
            )
            """.trimIndent()
        )

        insertarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS reservas")
        db.execSQL("DROP TABLE IF EXISTS maquinaria")
        db.execSQL("DROP TABLE IF EXISTS categorias")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("DROP TABLE IF EXISTS roles_opciones_menu")
        db.execSQL("DROP TABLE IF EXISTS opciones_menu")
        db.execSQL("DROP TABLE IF EXISTS roles")

        onCreate(db)
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO roles(nombre_rol, descripcion) VALUES ('Administrador', 'Gestiona maquinaria, usuarios y reservas')")
        db.execSQL("INSERT INTO roles(nombre_rol, descripcion) VALUES ('Cliente', 'Puede consultar catalogo y realizar reservas')")

        db.execSQL("INSERT INTO opciones_menu(nombre_opcion, descripcion) VALUES ('Gestionar maquinaria', 'Permite agregar, modificar y eliminar maquinaria')")
        db.execSQL("INSERT INTO opciones_menu(nombre_opcion, descripcion) VALUES ('Ver catalogo', 'Permite consultar maquinaria disponible')")
        db.execSQL("INSERT INTO opciones_menu(nombre_opcion, descripcion) VALUES ('Realizar reserva', 'Permite reservar maquinaria')")
        db.execSQL("INSERT INTO opciones_menu(nombre_opcion, descripcion) VALUES ('Historial de alquileres', 'Permite consultar reservas anteriores')")

        db.execSQL("INSERT INTO roles_opciones_menu(id_rol, id_opcion) VALUES (1, 1)")
        db.execSQL("INSERT INTO roles_opciones_menu(id_rol, id_opcion) VALUES (1, 2)")
        db.execSQL("INSERT INTO roles_opciones_menu(id_rol, id_opcion) VALUES (1, 3)")
        db.execSQL("INSERT INTO roles_opciones_menu(id_rol, id_opcion) VALUES (1, 4)")
        db.execSQL("INSERT INTO roles_opciones_menu(id_rol, id_opcion) VALUES (2, 2)")
        db.execSQL("INSERT INTO roles_opciones_menu(id_rol, id_opcion) VALUES (2, 3)")
        db.execSQL("INSERT INTO roles_opciones_menu(id_rol, id_opcion) VALUES (2, 4)")

        db.execSQL("INSERT INTO categorias(nombre_categoria, descripcion) VALUES ('Excavacion', 'Maquinaria para excavaciones y movimiento de tierra')")
        db.execSQL("INSERT INTO categorias(nombre_categoria, descripcion) VALUES ('Compactacion', 'Equipos para compactar suelo y superficies')")
        db.execSQL("INSERT INTO categorias(nombre_categoria, descripcion) VALUES ('Concreto', 'Equipos para preparacion y manejo de concreto')")

        db.execSQL(
            """
            INSERT INTO maquinaria(nombre, marca, modelo, capacidad, descripcion, costo_por_dia, estado, imagen, id_categoria)
            VALUES ('Retroexcavadora', 'CAT', '416F2', '1 m3', 'Equipo para excavacion y carga de material', 175.00, 'Disponible', '', 1)
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO maquinaria(nombre, marca, modelo, capacidad, descripcion, costo_por_dia, estado, imagen, id_categoria)
            VALUES ('Compactadora', 'Wacker Neuson', 'VP1550', '15 kN', 'Equipo para compactacion de suelo', 45.00, 'Disponible', '', 2)
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO usuarios(nombre, correo, telefono, contrasena, id_rol)
            VALUES ('Administrador', 'admin@maquinaria.com', '70000000', '1234', 1)
            """.trimIndent()
        )
    }
}