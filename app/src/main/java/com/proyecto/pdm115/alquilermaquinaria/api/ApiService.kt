package com.proyecto.pdm115.alquilermaquinaria.api

import com.proyecto.pdm115.alquilermaquinaria.models.ApiResponseMaquinaria
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    // Consulta la maquinaria desde la API PHP + MySQL
    @GET("maquinaria.php")
    fun obtenerMaquinariasRemotas(): Call<ApiResponseMaquinaria>
}