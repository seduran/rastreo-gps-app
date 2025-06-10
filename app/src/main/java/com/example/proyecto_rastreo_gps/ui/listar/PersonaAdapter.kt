package com.example.proyecto_rastreo_gps.ui.listar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto_rastreo_gps.R
import com.example.proyecto_rastreo_gps.ui.Persona

class PersonaAdapter(private var personas: List<Persona>) : RecyclerView.Adapter<PersonaAdapter.PersonaViewHolder>() {

    // ViewHolder que contiene las vistas para cada item
    class PersonaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreCliente: TextView = itemView.findViewById(R.id.tvNombreCliente)
        val codCli: TextView = itemView.findViewById(R.id.tvCodCli)
    }

    // Crea nuevas vistas (invocado por el layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_persona, parent, false)
        return PersonaViewHolder(view)
    }

    // Reemplaza el contenido de una vista (invocado por el layout manager)
    override fun onBindViewHolder(holder: PersonaViewHolder, position: Int) {
        val persona = personas[position]
        holder.nombreCliente.text = persona.cliente
        holder.codCli.text = "${persona.codcli} - "
    }

    // Devuelve el tamaño de tu dataset (invocado por el layout manager)
    override fun getItemCount() = personas.size

    // Función para actualizar la lista de personas en el adapter
    fun updateData(newPersonas: List<Persona>) {
        this.personas = newPersonas
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }
}