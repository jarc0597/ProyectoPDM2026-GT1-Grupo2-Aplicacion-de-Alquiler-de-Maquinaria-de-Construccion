package com.proyecto.pdm115.alquilermaquinaria.models

// Modelo que representa una maquinaria registrada en SQLite
data class Maquinaria(
    val idMaquinaria: Int,
    val codigoInterno: String,
    val nombreEquipo: String,
    val marca: String,
    val modelo: String,
    val capacidad: String?,
    val descripcion: String,
    val costoHora: Double,
    val costoDia: Double,
    val stock: Int,
    val imagenUrl: String?,
    val idCategoria: Int,
    val nombreCategoria: String?,
    val idEstado: Int,
    val nombreEstado: String?,
    val activo: Int
)