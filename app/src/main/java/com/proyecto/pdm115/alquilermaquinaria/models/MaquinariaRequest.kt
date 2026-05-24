package com.proyecto.pdm115.alquilermaquinaria.models

import com.google.gson.annotations.SerializedName

data class MaquinariaRequest(
    @SerializedName("codigo_interno")
    val codigoInterno: String,

    @SerializedName("nombre_equipo")
    val nombreEquipo: String,

    val marca: String,
    val modelo: String,
    val capacidad: String?,
    val descripcion: String,

    @SerializedName("costo_hora")
    val costoHora: Double,

    @SerializedName("costo_dia")
    val costoDia: Double,

    val stock: Int,

    @SerializedName("imagen_url")
    val imagenUrl: String,

    @SerializedName("id_categoria")
    val idCategoria: Int,

    @SerializedName("id_estado")
    val idEstado: Int
)