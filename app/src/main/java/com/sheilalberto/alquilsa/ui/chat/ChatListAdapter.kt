package com.sheilalberto.alquilsa.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Chat

class ChatListAdapter(private val context: Context, private val listaChat: ArrayList<Chat>):
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>(){


    private val MENSAJE_IZQUIERDA = 0
    private val MENSAJE_DERECHA = 1

    var firebaseUser: FirebaseUser? = null

    var idUsuario = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType == MENSAJE_DERECHA){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_derecha, parent, false)
            return ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_izquierda, parent, false)
            return ViewHolder(view)
        }


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = listaChat[position]
        holder.tvItemNombre.text = chat.mensaje

    }



    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaChat.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaChat.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Chat, position: Int) {
        listaChat.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaChat.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaChat.size
    }

    //Rescatamos los et y tv del item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvItemNombre : TextView = itemView.findViewById(R.id.tvMensaje)

    }

    override fun getItemViewType(position: Int): Int {

        firebaseUser = FirebaseAuth.getInstance().currentUser
        if(listaChat[position].idEmisor == firebaseUser!!.uid){
            return MENSAJE_DERECHA
        } else {
            return MENSAJE_IZQUIERDA
        }

    }



}
