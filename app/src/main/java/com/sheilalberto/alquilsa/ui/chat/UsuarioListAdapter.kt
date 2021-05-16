package com.sheilalberto.alquilsa.ui.chat

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_contacto.view.*

class UsuarioListAdapter(
    private val listaUsuario: MutableList<Usuario>,
    private val accionPrincipal: (Usuario) -> Unit) : RecyclerView.Adapter<UsuarioListAdapter.LugarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        return LugarViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contacto, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {
        holder.tvItemNombre.text = listaUsuario[position].nombre

        Picasso.get().load(Uri.parse(listaUsuario[position].foto)).into(holder.imgItemContacto)


        holder.itemUsuario.setOnClickListener(){
            accionPrincipal(listaUsuario[position])
        }
    }



    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaUsuario.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaUsuario.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Usuario, position: Int) {
        listaUsuario.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaUsuario.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaUsuario.size
    }

    //Rescatamos los et y tv del item
    class LugarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgItemContacto = itemView.imgItemContacto
        var tvItemNombre = itemView.tvItemNombre
        var tvItemTemp = itemView.tvItemTemp


        var itemUsuario = itemView.itemUsuario
        var context = itemView.context
    }



}
