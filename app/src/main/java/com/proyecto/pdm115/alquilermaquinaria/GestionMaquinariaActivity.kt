package com.proyecto.pdm115.alquilermaquinaria

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.proyecto.pdm115.alquilermaquinaria.db.DBHelper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.proyecto.pdm115.alquilermaquinaria.models.Maquinaria
import com.proyecto.pdm115.alquilermaquinaria.api.RetrofitClient
import com.proyecto.pdm115.alquilermaquinaria.models.ApiResponseSimple
import com.proyecto.pdm115.alquilermaquinaria.models.MaquinariaRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GestionMaquinariaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper


    private lateinit var spMaquinaria: Spinner
    private var listaMaquinarias: List<Maquinaria> = emptyList()
    private lateinit var etIdMaquinaria: EditText
    private lateinit var etCodigoInterno: EditText
    private lateinit var etNombreEquipo: EditText
    private lateinit var etMarca: EditText
    private lateinit var etModelo: EditText
    private lateinit var etCapacidad: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etCostoHora: EditText
    private lateinit var etCostoDia: EditText
    private lateinit var etStock: EditText
    private lateinit var etIdCategoria: EditText
    private lateinit var etIdEstado: EditText

    private lateinit var btnGuardar: Button
    private lateinit var btnActualizar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnLimpiar: Button


    private lateinit var btnBuscar: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga la pantalla de gestión de maquinaria
        setContentView(R.layout.activity_gestion_maquinaria)

        // Inicializa la conexión con SQLite
        dbHelper = DBHelper(this)

        // Enlaza los campos del XML con Kotlin
        inicializarVistas()

        // Activa las acciones de los botones
        configurarEventos()

        cargarSelectorMaquinarias()
    }

    private fun inicializarVistas() {
        spMaquinaria = findViewById(R.id.sp_maquinaria)

        etIdMaquinaria = findViewById(R.id.et_id_maquinaria)
        etCodigoInterno = findViewById(R.id.et_codigo_interno)
        etNombreEquipo = findViewById(R.id.et_nombre_equipo)
        etMarca = findViewById(R.id.et_marca)
        etModelo = findViewById(R.id.et_modelo)
        etCapacidad = findViewById(R.id.et_capacidad)
        etDescripcion = findViewById(R.id.et_descripcion)
        etCostoHora = findViewById(R.id.et_costo_hora)
        etCostoDia = findViewById(R.id.et_costo_dia)
        etStock = findViewById(R.id.et_stock)
        etIdCategoria = findViewById(R.id.et_id_categoria)
        etIdEstado = findViewById(R.id.et_id_estado)

        btnGuardar = findViewById(R.id.btn_guardar_maquinaria)
        btnActualizar = findViewById(R.id.btn_actualizar_maquinaria)
        btnEliminar = findViewById(R.id.btn_eliminar_maquinaria)
        btnLimpiar = findViewById(R.id.btn_limpiar_maquinaria)
        btnBuscar = findViewById(R.id.btn_buscar_maquinaria)
    }

    private fun configurarEventos() {
        btnGuardar.setOnClickListener {
            guardarMaquinaria()
        }

        btnActualizar.setOnClickListener {
            actualizarMaquinaria()
        }

        btnEliminar.setOnClickListener {
            eliminarMaquinaria()
        }

        btnLimpiar.setOnClickListener {
            limpiarFormulario()
        }

        btnBuscar.setOnClickListener {
            buscarMaquinariaPorCodigo()
        }
    }

    private fun guardarMaquinaria() {
        if (!validarCamposBasicos()) return

        // Objeto que se guardará localmente y también se enviará a la API
        val maquinariaRequest = MaquinariaRequest(
            codigoInterno = etCodigoInterno.text.toString().trim(),
            nombreEquipo = etNombreEquipo.text.toString().trim(),
            marca = etMarca.text.toString().trim(),
            modelo = etModelo.text.toString().trim(),
            capacidad = etCapacidad.text.toString().trim(),
            descripcion = etDescripcion.text.toString().trim(),
            costoHora = etCostoHora.text.toString().toDouble(),
            costoDia = etCostoDia.text.toString().toDouble(),
            stock = etStock.text.toString().toInt(),
            imagenUrl = "",
            idCategoria = etIdCategoria.text.toString().toInt(),
            idEstado = etIdEstado.text.toString().toInt()
        )

        // Primero se guarda en SQLite local
        val resultado = dbHelper.insertarMaquinaria(
            codigoInterno = maquinariaRequest.codigoInterno,
            nombreEquipo = maquinariaRequest.nombreEquipo,
            marca = maquinariaRequest.marca,
            modelo = maquinariaRequest.modelo,
            capacidad = maquinariaRequest.capacidad,
            descripcion = maquinariaRequest.descripcion,
            costoHora = maquinariaRequest.costoHora,
            costoDia = maquinariaRequest.costoDia,
            stock = maquinariaRequest.stock,
            imagenUrl = maquinariaRequest.imagenUrl,
            idCategoria = maquinariaRequest.idCategoria,
            idEstado = maquinariaRequest.idEstado
        )

        if (resultado > 0) {
            Toast.makeText(
                this,
                "Maquinaria guardada localmente. Enviando a MySQL...",
                Toast.LENGTH_LONG
            ).show()

            // Luego se envía a MySQL mediante la API
            enviarMaquinariaApi(maquinariaRequest)

            limpiarFormulario()
            cargarSelectorMaquinarias()
        } else {
            Toast.makeText(
                this,
                "No se pudo guardar la maquinaria",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun enviarMaquinariaApi(maquinariaRequest: MaquinariaRequest) {
        RetrofitClient.apiService.insertarMaquinariaRemota(maquinariaRequest)
            .enqueue(object : Callback<ApiResponseSimple> {

                override fun onResponse(
                    call: Call<ApiResponseSimple>,
                    response: Response<ApiResponseSimple>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(
                            this@GestionMaquinariaActivity,
                            "Guardada localmente, pero API devolvió error HTTP: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val respuesta = response.body()

                    if (respuesta != null && respuesta.ok) {
                        Toast.makeText(
                            this@GestionMaquinariaActivity,
                            "Maquinaria enviada a MySQL correctamente",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@GestionMaquinariaActivity,
                            "Guardada localmente, pero la API no confirmó inserción",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponseSimple>, t: Throwable) {
                    Toast.makeText(
                        this@GestionMaquinariaActivity,
                        "Guardada localmente, pero no se pudo conectar con API: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun actualizarMaquinaria() {
        val idMaquinaria = etIdMaquinaria.text.toString().trim()

        if (idMaquinaria.isEmpty()) {
            etIdMaquinaria.error = "Ingrese el ID de la maquinaria"
            return
        }

        if (!validarCamposBasicos()) return

        val filasActualizadas = dbHelper.actualizarMaquinaria(
            idMaquinaria = idMaquinaria.toInt(),
            codigoInterno = etCodigoInterno.text.toString().trim(),
            nombreEquipo = etNombreEquipo.text.toString().trim(),
            marca = etMarca.text.toString().trim(),
            modelo = etModelo.text.toString().trim(),
            capacidad = etCapacidad.text.toString().trim(),
            descripcion = etDescripcion.text.toString().trim(),
            costoHora = etCostoHora.text.toString().toDouble(),
            costoDia = etCostoDia.text.toString().toDouble(),
            stock = etStock.text.toString().toInt(),
            imagenUrl = "",
            idCategoria = etIdCategoria.text.toString().toInt(),
            idEstado = etIdEstado.text.toString().toInt()
        )

        if (filasActualizadas > 0) {
            Toast.makeText(this, "Maquinaria actualizada correctamente", Toast.LENGTH_LONG).show()
            limpiarFormulario()
            cargarSelectorMaquinarias()
        } else {
            Toast.makeText(this, "No se encontró la maquinaria a actualizar", Toast.LENGTH_LONG).show()
        }
    }

    private fun eliminarMaquinaria() {
        val idMaquinaria = etIdMaquinaria.text.toString().trim()

        if (idMaquinaria.isEmpty()) {
            etIdMaquinaria.error = "Ingrese el ID de la maquinaria"
            return
        }

        val resultado = dbHelper.eliminarMaquinaria(idMaquinaria.toInt())

        when {
            resultado > 0 -> {
                Toast.makeText(
                    this,
                    "Maquinaria eliminada correctamente",
                    Toast.LENGTH_LONG
                ).show()

                limpiarFormulario()
                cargarSelectorMaquinarias()
            }

            resultado == -2 -> {
                Toast.makeText(
                    this,
                    "No se puede eliminar. La maquinaria tiene reservas activas.",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    this,
                    "No se encontró la maquinaria a eliminar",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validarCamposBasicos(): Boolean {
        if (etCodigoInterno.text.toString().trim().isEmpty()) {
            etCodigoInterno.error = "Ingrese el código interno"
            return false
        }

        if (etNombreEquipo.text.toString().trim().isEmpty()) {
            etNombreEquipo.error = "Ingrese el nombre del equipo"
            return false
        }

        if (etMarca.text.toString().trim().isEmpty()) {
            etMarca.error = "Ingrese la marca"
            return false
        }

        if (etModelo.text.toString().trim().isEmpty()) {
            etModelo.error = "Ingrese el modelo"
            return false
        }

        if (etDescripcion.text.toString().trim().isEmpty()) {
            etDescripcion.error = "Ingrese la descripción"
            return false
        }

        if (etCostoHora.text.toString().trim().isEmpty()) {
            etCostoHora.error = "Ingrese el costo por hora"
            return false
        }

        if (etCostoDia.text.toString().trim().isEmpty()) {
            etCostoDia.error = "Ingrese el costo por día"
            return false
        }

        if (etStock.text.toString().trim().isEmpty()) {
            etStock.error = "Ingrese el stock"
            return false
        }

        if (etIdCategoria.text.toString().trim().isEmpty()) {
            etIdCategoria.error = "Ingrese el ID de categoría"
            return false
        }

        if (etIdEstado.text.toString().trim().isEmpty()) {
            etIdEstado.error = "Ingrese el ID de estado"
            return false
        }

        return true
    }

    private fun limpiarFormulario() {
        etIdMaquinaria.text.clear()
        etCodigoInterno.text.clear()
        etNombreEquipo.text.clear()
        etMarca.text.clear()
        etModelo.text.clear()
        etCapacidad.text.clear()
        etDescripcion.text.clear()
        etCostoHora.text.clear()
        etCostoDia.text.clear()
        etStock.text.clear()
        etIdCategoria.text.clear()
        etIdEstado.text.clear()
    }
    private fun buscarMaquinariaPorCodigo() {
        val codigo = etCodigoInterno.text.toString().trim()

        if (codigo.isEmpty()) {
            etCodigoInterno.error = "Ingrese el código interno"
            return
        }

        val maquinaria = dbHelper.obtenerMaquinariaPorCodigo(codigo)

        if (maquinaria == null) {
            Toast.makeText(this, "No se encontró maquinaria con ese código", Toast.LENGTH_LONG).show()
            return
        }

        // Llena automáticamente el formulario con los datos encontrados
        etIdMaquinaria.setText(maquinaria.idMaquinaria.toString())
        etCodigoInterno.setText(maquinaria.codigoInterno)
        etNombreEquipo.setText(maquinaria.nombreEquipo)
        etMarca.setText(maquinaria.marca)
        etModelo.setText(maquinaria.modelo)
        etCapacidad.setText(maquinaria.capacidad ?: "")
        etDescripcion.setText(maquinaria.descripcion)
        etCostoHora.setText(maquinaria.costoHora.toString())
        etCostoDia.setText(maquinaria.costoDia.toString())
        etStock.setText(maquinaria.stock.toString())
        etIdCategoria.setText(maquinaria.idCategoria.toString())
        etIdEstado.setText(maquinaria.idEstado.toString())

        Toast.makeText(this, "Maquinaria encontrada", Toast.LENGTH_LONG).show()
    }
    private fun cargarSelectorMaquinarias() {
        listaMaquinarias = dbHelper.obtenerMaquinarias()

        val nombres = mutableListOf("Seleccione una maquinaria")
        nombres.addAll(listaMaquinarias.map { "${it.codigoInterno} - ${it.nombreEquipo}" })

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombres
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spMaquinaria.adapter = adapter

        spMaquinaria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) return

                val maquinariaSeleccionada = listaMaquinarias[position - 1]
                llenarFormularioConMaquinaria(maquinariaSeleccionada)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se requiere acción
            }
        }
    }

    private fun llenarFormularioConMaquinaria(maquinaria: Maquinaria) {
        etIdMaquinaria.setText(maquinaria.idMaquinaria.toString())
        etCodigoInterno.setText(maquinaria.codigoInterno)
        etNombreEquipo.setText(maquinaria.nombreEquipo)
        etMarca.setText(maquinaria.marca)
        etModelo.setText(maquinaria.modelo)
        etCapacidad.setText(maquinaria.capacidad ?: "")
        etDescripcion.setText(maquinaria.descripcion)
        etCostoHora.setText(maquinaria.costoHora.toString())
        etCostoDia.setText(maquinaria.costoDia.toString())
        etStock.setText(maquinaria.stock.toString())
        etIdCategoria.setText(maquinaria.idCategoria.toString())
        etIdEstado.setText(maquinaria.idEstado.toString())
    }
}