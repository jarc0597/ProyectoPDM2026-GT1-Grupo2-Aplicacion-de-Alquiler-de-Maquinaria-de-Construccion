package com.proyecto.pdm115.alquilermaquinaria.models

data class ApiResponseMaquinaria(
    val ok: Boolean,
    val mensaje: String,
    val data: List<MaquinariaRemota>
)