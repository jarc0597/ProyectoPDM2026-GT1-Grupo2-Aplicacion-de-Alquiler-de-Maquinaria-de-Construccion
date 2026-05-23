package com.proyecto.pdm115.alquilermaquinaria

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.proyecto.pdm115.alquilermaquinaria.api.RetrofitClient
import com.proyecto.pdm115.alquilermaquinaria.models.ApiResponseMaquinaria
import com.proyecto.pdm115.alquilermaquinaria.models.MaquinariaRemota
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaquinariaRemotaActivity : AppCompatActivity() {

    private lateinit var tvEstadoApi: TextView
    private lateinit var btnRecargarApi: Button
    private lateinit var btnVolverApi: Button
    private lateinit var contenedorMaquinariaRemota: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga la pantalla que mostrará los datos desde la API
        setContentView(R.layout.activity_maquinaria_remota)

        inicializarVistas()
        configurarEventos()

        // Consulta inicial a la API
        consultarMaquinariasApi()
    }

    private fun inicializarVistas() {
        tvEstadoApi = findViewById(R.id.tv_estado_api)
        btnRecargarApi = findViewById(R.id.btn_recargar_api)
        btnVolverApi = findViewById(R.id.btn_volver_api)
        contenedorMaquinariaRemota = findViewById(R.id.contenedor_maquinaria_remota)
    }

    private fun configurarEventos() {
        btnRecargarApi.setOnClickListener {
            consultarMaquinariasApi()
        }

        btnVolverApi.setOnClickListener {
            finish()
        }
    }

    private fun consultarMaquinariasApi() {
        tvEstadoApi.text = "Consultando API..."
        contenedorMaquinariaRemota.removeAllViews()

        RetrofitClient.apiService.obtenerMaquinariasRemotas()
            .enqueue(object : Callback<ApiResponseMaquinaria> {

                override fun onResponse(
                    call: Call<ApiResponseMaquinaria>,
                    response: Response<ApiResponseMaquinaria>
                ) {
                    if (!response.isSuccessful) {
                        tvEstadoApi.text = "Error HTTP: ${response.code()}"
                        return
                    }

                    val respuesta = response.body()

                    if (respuesta == null || !respuesta.ok) {
                        tvEstadoApi.text = "La API no devolvió datos válidos"
                        return
                    }

                    val lista = respuesta.data

                    tvEstadoApi.text = "Registros recibidos desde API: ${lista.size}"
                    mostrarMaquinarias(lista)
                }

                override fun onFailure(call: Call<ApiResponseMaquinaria>, t: Throwable) {
                    tvEstadoApi.text = "Error al conectar con API: ${t.message}"
                }
            })
    }

    private fun mostrarMaquinarias(lista: List<MaquinariaRemota>) {
        contenedorMaquinariaRemota.removeAllViews()

        if (lista.isEmpty()) {
            val tvVacio = TextView(this).apply {
                text = "No hay maquinaria remota registrada."
                textSize = 16f
                setTextColor(android.graphics.Color.parseColor("#64748B"))
                setPadding(0, 20, 0, 20)
            }

            contenedorMaquinariaRemota.addView(tvVacio)
            return
        }

        for (maquinaria in lista) {
            val tarjeta = TextView(this).apply {
                text = """
                    Código: ${maquinaria.codigoInterno}
                    Equipo: ${maquinaria.nombreEquipo}
                    Marca: ${maquinaria.marca}
                    Modelo: ${maquinaria.modelo}
                    Categoría: ${maquinaria.nombreCategoria ?: "Sin categoría"}
                    Estado: ${maquinaria.nombreEstado ?: "Sin estado"}
                    Costo día: $${maquinaria.costoDia}
                    Costo hora: $${maquinaria.costoHora}
                    Stock: ${maquinaria.stock}
                """.trimIndent()

                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#0B1C30"))
                setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
                setPadding(24, 24, 24, 24)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(0, 0, 0, 20)
            tarjeta.layoutParams = params

            contenedorMaquinariaRemota.addView(tarjeta)
        }
    }
}