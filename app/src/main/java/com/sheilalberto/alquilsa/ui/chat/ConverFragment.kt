package com.sheilalberto.alquilsa.ui.chat

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.sheilalberto.alquilsa.CirculoTransformacion
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Chat
import com.sheilalberto.alquilsa.clases.Usuario
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class ConverFragment(val u: Usuario) : Fragment() {


    //Creacion de variables
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    var chatList = ArrayList<Chat>()

    var idUsuarioReceptor = ""
    var idUsuarioEmisor = ""

    private lateinit var recy : RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_conver, container, false)

        //Recogemos los componentes del layout en las variables
        var imgConverFoto : CircleImageView = root.findViewById(R.id.imgConverFoto)
        var imgConverEnviar: ImageView = root.findViewById(R.id.imaConverEnviar)
        var tvConverNombre: TextView = root.findViewById(R.id.tvConverNombre)
        var etConverMensaje : EditText = root.findViewById(R.id.etConverMandar)

        recy = root.findViewById(R.id.coverRecycler)

        val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        recy.layoutManager = LinearLayoutManager(context)

        //Ponemos como idUsuarioEmisor nuestro id, porque somos la persona que nos hemos
        //logueado y quienes vamos a mandar mensajes desde nuestra app
        idUsuarioEmisor = prefs?.getString("idUsuario", "null").toString()


        //Como idUsuarioReceptor ponemos el id del usuario que recibimos en esta clase, que será
        //el id del usuario que se encontraba en la posición en la que hemos hecho click para abrir
        //un chat con el
        idUsuarioReceptor = u.idUsuario


        //Recogemos y ponemos la foto y el nombre del usuario que recibimos (con el que vamos a hablar)
        Picasso.get().load(Uri.parse(u.foto)).transform(CirculoTransformacion()).into(imgConverFoto)
        tvConverNombre.text = u.nombre

        //Si pulsamos el boton enviar
        imgConverEnviar.setOnClickListener{
            var mensaje = etConverMensaje.text.toString()

            //Si el mensaje está vacio
            if (mensaje.isEmpty()){
                etConverMensaje.setText("")
                //En caso de que el mensaje no este vacio, llamamos al metodo enviarMensaje(...)
            } else {
                enviarMensaje(idUsuarioEmisor, idUsuarioReceptor, mensaje)
                etConverMensaje.setText("")
            }
        }

        leerMensaje(idUsuarioEmisor, idUsuarioReceptor)

        return root
    }
    /*
    Metodo que recibe, el idEmisor, idReceptor y el mensaje para poder guardarlo en la bbdd
     */
    private fun enviarMensaje(idEmisor: String, idReceptor: String, mensaje: String){
        //Instanciamos la base de datos
        db = FirebaseDatabase.getInstance("https://alquilsaguarro-default-rtdb.europe-west1.firebasedatabase.app/")
        databaseReference = db.reference.child("chat")//Tabla chat

        var hasMap : HashMap<String, String> = HashMap()
        hasMap.put("idEmisor", idEmisor)
        hasMap.put("idReceptor", idReceptor)
        hasMap.put("mensaje", mensaje)

        databaseReference.push().setValue(hasMap)
    }

    /*
    Metodo que recibe el idUsuarioEmisor e idUsuarioReceptor para poder hacer una consulta
    en la base de datos y asi poder rescatar el mensaje
     */
    private fun leerMensaje(idEmisor: String, idReceptor: String){

        //Instanciamos la bbdd
        db = FirebaseDatabase.getInstance("https://alquilsaguarro-default-rtdb.europe-west1.firebasedatabase.app/")
        databaseReference = db.reference.child("chat")//Tabla chat

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                chatList.clear()

                snapshot.children.forEach {

                    //Si encontramos en la bbdd que coincidan el idUsuarioReceptor y el idUsuarioEmisor con los campos de la bbdd
                    //idEmisor e idReceptor es señal de que ha habido intercambio de mensajes entre ellos
                    if(it.child("idEmisor").getValue().toString().equals(idEmisor) && it.child("idReceptor").getValue().toString().equals(idReceptor) ||
                        it.child("idEmisor").getValue().toString().equals(idReceptor) && it.child("idReceptor").getValue().toString().equals(idEmisor)){

                        //Rescatamos de la base de datos, el emisor, el receptor y el mensaje y creamos un objeto con esos parametros recogidos
                        val idEmisor = it.child("idEmisor").getValue().toString()
                        val idReceptor = it.child("idReceptor").getValue().toString()
                        val mensaje = it.child("mensaje").getValue().toString()
                        Log.e("MENSAJE" , "AAA" + mensaje)
                        val c = Chat(idEmisor, idReceptor, mensaje)
                        //Añadimos ese objeto a la lista de chat
                        chatList.add(c)
                    }

                }

                val chatAdapter = ChatListAdapter(requireContext(), chatList)

                recy.adapter = chatAdapter

            }



            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }


}