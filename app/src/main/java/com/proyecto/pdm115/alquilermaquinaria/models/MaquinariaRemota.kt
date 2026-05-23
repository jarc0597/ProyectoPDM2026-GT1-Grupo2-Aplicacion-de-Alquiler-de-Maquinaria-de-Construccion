package com.proyecto.pdm115.alquilermaquinaria.models

import com.google.gson.annotations.SerializedName

data class MaquinariaRemota(
    @SerializedName("id_maquinaria")
    val idMaquinaria: Int,

    @SerializedName("codigo_interno")
    val codigoInterno: String,

    @SerializedName("nombre_equipo")
    val nombreEquipo: String,

    val marca: String,
    val modelo: String,
    val capacidad: String?,
    val descripcion: String,

    @SerializedName("costo_hora")
    val costoHora: String,

    @SerializedName("costo_dia")
    val costoDia: String,

    val stock: Int,

    @SerializedName("imagen_url")
    val imagenUrl: String?,

    @SerializedName("id_categoria")
    val idCategoria: Int,

    @SerializedName("nombre_categoria")
    val nombreCategoria: String?,

    @SerializedName("id_estado")
    val idEstado: Int,

    @SerializedName("nombre_estado")
    val nombreEstado: String?,

    val activo: Int
)