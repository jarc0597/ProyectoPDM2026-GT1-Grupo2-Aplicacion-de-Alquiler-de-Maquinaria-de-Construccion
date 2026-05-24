package com.proyecto.pdm115.alquilermaquinaria.api

import com.proyecto.pdm115.alquilermaquinaria.models.ApiResponseMaquinaria
import com.proyecto.pdm115.alquilermaquinaria.models.ApiResponseSimple
import com.proyecto.pdm115.alquilermaquinaria.models.MaquinariaRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // Consulta la maquinaria desde la API PHP + MySQL
    @GET("maquinaria.php")
    fun obtenerMaquinariasRemotas(): Call<ApiResponseMaquinaria>

    // Inserta maquinaria en MySQL usando la API PHP
    @POST("maquinaria.php")
    fun insertarMaquinariaRemota(
        @Body maquinaria: MaquinariaRequest
    ): Call<ApiResponseSimple>
}