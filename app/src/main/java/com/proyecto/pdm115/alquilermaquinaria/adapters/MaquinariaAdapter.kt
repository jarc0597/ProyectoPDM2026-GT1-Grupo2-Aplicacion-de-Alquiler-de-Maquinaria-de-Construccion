package com.proyecto.pdm115.alquilermaquinaria.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.proyecto.pdm115.alquilermaquinaria.R
import com.proyecto.pdm115.alquilermaquinaria.activity_detalle_maquinaria
import com.proyecto.pdm115.alquilermaquinaria.models.Maquinaria

class MaquinariaAdapter(
    private val listaMaquinaria: List<Maquinaria>
) : RecyclerView.Adapter<MaquinariaAdapter.MaquinariaViewHolder>() {

    class MaquinariaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMaquina: ImageView = itemView.findViewById(R.id.img_maquina)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        val tvNombreMaquina: TextView = itemView.findViewById(R.id.tv_nombre_maquina)
        val tvUbicacion: TextView = itemView.findViewById(R.id.tv_ubicacion)
        val tvPrecio: TextView = itemView.findViewById(R.id.tv_precio)
        val btnAdd: MaterialButton = itemView.findViewById(R.id.btn_add)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaquinariaViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_maquinas, parent, false)

        return MaquinariaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MaquinariaViewHolder, position: Int) {
        val maquinaria = listaMaquinaria[position]

        // Datos principales de la maquinaria
        holder.tvNombreMaquina.text = maquinaria.nombreEquipo
        holder.tvStatus.text = maquinaria.nombreEstado ?: "Sin estado"

        // Usamos este campo para mostrar marca, modelo y categoria
        holder.tvUbicacion.text = "${maquinaria.marca} ${maquinaria.modelo} - ${maquinaria.nombreCategoria}"

        // Precio diario mostrado en formato simple
        holder.tvPrecio.text = "$${String.format("%.2f", maquinaria.costoDia)}"

        // Imagen temporal mientras no tengamos imagenes reales por equipo
        holder.imgMaquina.setImageResource(R.drawable.backgrounf_hero)

        // Al tocar la tarjeta, abre el detalle de maquinaria
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, activity_detalle_maquinaria::class.java)
            holder.itemView.context.startActivity(intent)
        }

        // Boton de agregar o reservar
        holder.btnAdd.setOnClickListener {
            val intent = Intent(holder.itemView.context, activity_detalle_maquinaria::class.java)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listaMaquinaria.size
    }
}