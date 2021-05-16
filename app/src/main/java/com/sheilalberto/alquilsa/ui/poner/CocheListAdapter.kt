package com.sheilalberto.alquilsa.ui.poner

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Coche
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_coche.view.*

class CocheListAdapter(
        private val listaCoche: MutableList<Coche>,
        private val accionPrincipal: (Coche) -> Unit

) : RecyclerView.Adapter<CocheListAdapter.LugarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        return LugarViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_coche, parent, false)
        )
    }

    //Rescatamos los datos de un coche y lo ponemos en sus componentes
    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {

        holder.tvItemCocheNombre.text = listaCoche[position].nombre
        holder.tvItemCocheAsientos.text = listaCoche[position].asientos
        holder.tvItemCocheCombustible.text = listaCoche[position].combustible
        holder.tvItemCocheAutonomia.text = listaCoche[position].autonomia
        holder.tvItemCocheTransmision.text = listaCoche[position].transmision
        holder.tvItemCocheLikes.text = listaCoche[position].likes.toString()
        holder.tvItemCocheMatricula.text = listaCoche[position].matricula

        //Si el valor de disponible es true, se pone un fondo al layout, si no, se pone otro
        if (listaCoche[position].disponible == true){
            holder.tvItemCocheDisponibilidad.setBackgroundResource(R.drawable.fondo_disponible)
        } else {
            holder.tvItemCocheDisponibilidad.setBackgroundResource(R.drawable.fondo_no_disponible)
        }


        Picasso.get().load(Uri.parse(listaCoche[position].foto)).into(holder.imaItemCocheFoto)

        holder.itemCoches.setOnClickListener(){
            accionPrincipal(listaCoche[position])
        }
    }


    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaCoche.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaCoche.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Coche, position: Int) {
        listaCoche.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaCoche.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaCoche.size
    }

    //Rescatamos los tv y las imágenes del layout
    class LugarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvItemCocheNombre = itemView.tvCocheNombre
        var tvItemCocheLikes = itemView.tvCocheLike
        var tvItemCocheAsientos = itemView.tvCochePerson
        var tvItemCocheCombustible = itemView.tvCocheGasolina
        var tvItemCocheTransmision = itemView.tvCocheMarcha
        var tvItemCocheAutonomia = itemView.tvCocheKm
        var tvItemCocheMatricula = itemView.tvCocheMatricula
        var tvItemCocheDisponibilidad = itemView.tvCocheDisponibilidad
        var imaItemCocheFoto = itemView.imaCocheFoto

        var itemCoches = itemView.itemCoche
        var context = itemView.context
    }

}