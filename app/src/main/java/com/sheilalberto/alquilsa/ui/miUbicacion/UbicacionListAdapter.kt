package com.sheilalberto.alquilsa.ui.miUbicacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Ubicacion
import kotlinx.android.synthetic.main.item_ubicacion.view.*

class UbicacionListAdapter(
    private val listaUbicacion: MutableList<Ubicacion>,
    private val accionPrincipal: (Ubicacion) -> Unit

) : RecyclerView.Adapter<UbicacionListAdapter.LugarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        return LugarViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ubicacion, parent, false)
        )
    }

    //Rescatamos los datos de una ubicacion y los ponemos en sus componentes
    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {

        holder.tvItemUbicacionNombre.text = listaUbicacion[position].nombre
        holder.tvItemUbicacionLatitud.text = listaUbicacion[position].latitud
        holder.tvItemUbicacionLongitud.text = listaUbicacion[position].longitud

        holder.itemSitios.setOnClickListener(){
            accionPrincipal(listaUbicacion[position])
        }
    }


    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaUbicacion.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaUbicacion.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Ubicacion, position: Int) {
        listaUbicacion.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaUbicacion.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaUbicacion.size
    }

    //Rescatamos los tv
    class LugarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvItemUbicacionNombre = itemView.tvItemUbicacionNombre
        var tvItemUbicacionLatitud = itemView.tvItemUbicacionlatitud
        var tvItemUbicacionLongitud = itemView.tvItemUbicacionLongitud


        var itemSitios = itemView.itemUbicacion
        var context = itemView.context
    }

}
