package com.proyecto.pdm115.alquilermaquinaria.models

data class ApiResponseSimple(
    val ok: Boolean,
    val mensaje: String,
    val data: Map<String, Any>? = null
)