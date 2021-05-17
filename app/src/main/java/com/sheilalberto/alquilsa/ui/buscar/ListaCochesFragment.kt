package com.sheilalberto.alquilsa.ui.buscar

import android.graphics.Paint
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sheilalberto.alquilsa.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.sheilalberto.alquilsa.CirculoTransformacion
import com.sheilalberto.alquilsa.clases.Coche
import com.sheilalberto.alquilsa.ui.poner.CocheListAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_lista2_coches.*

class ListaCochesFragment(
        private val fabBuscarFiltro: FloatingActionButton,
        private val idUbicacion : String,
        listaIdsCoches: MutableList<String> = mutableListOf<String>()
) : Fragment() {

    private var listaCochesFiltro = listaIdsCoches
    private var fabBoton = fabBuscarFiltro
    private var idUbica = idUbicacion

    private lateinit var imgListaCoche : ImageView
    private lateinit var tvListaNombre : TextView
    private lateinit var tvListaEmail : TextView
    private lateinit var tvListaTelefono : TextView

    private var listaCoches = mutableListOf<Coche>() //Lista de coches
    private lateinit var cocheAdapter: CocheListAdapter //Adaptador de coches
    private lateinit var tareaCoche: ListaCochesFragment.TareaCargarCoches // Tarea hilo para cargar coches
    private var paintSweep = Paint()
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseUsuariosReference: DatabaseReference

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_lista2_coches, container, false)

        imgListaCoche = root.findViewById(R.id.ivListaCoche)
        tvListaNombre = root.findViewById(R.id.tvListaNombreUsu)
        tvListaEmail = root.findViewById(R.id.tvListaEmail)
        tvListaTelefono = root.findViewById(R.id.tvListaTelefono)

        fabBoton.hide()


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cochesListaRecycler.layoutManager = LinearLayoutManager(context)


        iniciarSwipeRecarga()
        //iniciarSwipeHorizontal()
        cargarCoches()
        Log.e("CARGAR", "CARGADAAAAS")
        visualizarListaItems()

    }

    /**
     * Swipe recarga de coches
     */
    private fun iniciarSwipeRecarga() {

        cochesListaSwipeRefresh.setColorSchemeResources(R.color.principal_oscuro)
        cochesListaSwipeRefresh.setProgressBackgroundColorSchemeResource(R.color.secundario)
        cochesListaSwipeRefresh.setOnRefreshListener {

            //cargamos los coches al refrescar
            cargarCoches()
        }
    }

    /**
     * LLamamos al hilo para cargar los coches
     */
    private fun cargarCoches() {
        tareaCoche = TareaCargarCoches()
        tareaCoche.execute()
    }

    /**
     * Función que devuelve la lista de coches que queremos mostrar en el recycler
     */
    private fun rellenarArrayCoche() : MutableList<Coche> {

        var idUsuario = ""
        var lista = mutableListOf<Coche>() //Lista de sitios

        db = FirebaseDatabase.getInstance("https://alquilsa-default-rtdb.europe-west1.firebasedatabase.app/")

        //cogemos la referencia a las tablas de coches y usuarios
        databaseReference = db.reference.child("coches")
        databaseUsuariosReference = db.reference.child("usuarios")

        //Select que devuelve la lista de los coches que pertenecen a la ubicación seleccionada
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {

                    if (it.child("idUbicacion").getValue().toString() == idUbicacion) {

                        val nombre = it.child("nombre").getValue().toString()
                        val asientos = it.child("asientos").getValue().toString()
                        val autonomia = it.child("autonomia").getValue().toString()
                        val combustible = it.child("combustible").getValue().toString()
                        val disponible = it.child("disponible").getValue().toString().toBoolean()
                        val foto = it.child("foto").getValue().toString()
                        val idCoche = it.child("idCoche").getValue().toString()
                        val idUbicacion = it.child("idUbicacion").getValue().toString()
                        val idU = it.child("idUsuario").getValue().toString()
                        val likes = it.child("likes").getValue().toString().toInt()
                        val matricula = it.child("matricula").getValue().toString()
                        val precio = it.child("precio").getValue().toString().toFloat()
                        val tipo = it.child("tipo").getValue().toString()
                        val transmision = it.child("transmision").getValue().toString()

                        idUsuario = idU
                        val c = Coche(idCoche, matricula, idU, foto, nombre, likes, idUbicacion,
                                autonomia, combustible, transmision, asientos, tipo,
                                precio, disponible)

                        lista.add(c)
                    }
                }

                //Select que devuelve el usuario al que petenece la ubicación
                databaseUsuariosReference = db.reference.child("usuarios").child(idUsuario)
                databaseUsuariosReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val nombre = snapshot.child("nombre").getValue().toString()
                        val foto = snapshot.child("foto").getValue().toString()
                        val email = snapshot.child("email").getValue().toString()
                        val telefono = snapshot.child("telefono").getValue().toString()

                        //Cargamos al usuario en la parte superior de la pantalla
                        tvListaNombre.text = nombre
                        tvListaEmail.text = email
                        tvListaTelefono.text = telefono

                        Picasso.get()
                                // .load(R.drawable.user_avatar)
                                .load(Uri.parse(foto))
                                .transform(CirculoTransformacion())
                                .resize(178, 178)
                                .into(imgListaCoche)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })



            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        return lista

    }

    /**
     * Función que devuelve los coches que queremos mostrar en el recycler si viene del filtro
     */
    private fun rellenarArrayFiltro() : MutableList<Coche> {

        var idUsuario = ""
        var lista = mutableListOf<Coche>() //Lista de coches

        db = FirebaseDatabase.getInstance("https://alquilsa-default-rtdb.europe-west1.firebasedatabase.app/")

        //referencias a las tablas de coches y usuarios
        databaseReference = db.reference.child("coches")
        databaseUsuariosReference = db.reference.child("usuarios")

        //Select que devuelve los cooches que pertenecen a esa ubucación y están en la lista de coches que nos viene
        //desde el filtro
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    for (i in 0..listaCochesFiltro.size -1) {

                        if (it.child("idUbicacion").getValue().toString() == idUbicacion &&
                                listaCochesFiltro[i].toString() == it.child("idCoche").getValue().toString()) {

                            val nombre = it.child("nombre").getValue().toString()
                            val asientos = it.child("asientos").getValue().toString()
                            val autonomia = it.child("autonomia").getValue().toString()
                            val combustible = it.child("combustible").getValue().toString()
                            val disponible = it.child("disponible").getValue().toString().toBoolean()
                            val foto = it.child("foto").getValue().toString()
                            val idCoche = it.child("idCoche").getValue().toString()
                            val idUbicacion = it.child("idUbicacion").getValue().toString()
                            val idU = it.child("idUsuario").getValue().toString()
                            val likes = it.child("likes").getValue().toString().toInt()
                            val matricula = it.child("matricula").getValue().toString()
                            val precio = it.child("precio").getValue().toString().toFloat()
                            val tipo = it.child("tipo").getValue().toString()
                            val transmision = it.child("transmision").getValue().toString()

                            idUsuario = idU
                            val c = Coche(idCoche, matricula, idU, foto, nombre, likes, idUbicacion,
                                    autonomia, combustible, transmision, asientos, tipo,
                                    precio, disponible)

                            lista.add(c)
                        }

                    }
                }

                //Select que devuelve el usuario al que petenece la ubicación
                databaseUsuariosReference = db.reference.child("usuarios").child(idUsuario)
                databaseUsuariosReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val nombre = snapshot.child("nombre").getValue().toString()
                        val foto = snapshot.child("foto").getValue().toString()
                        val email = snapshot.child("email").getValue().toString()
                        val telefono = snapshot.child("telefono").getValue().toString()

                        tvListaNombre.text = nombre
                        tvListaEmail.text = email
                        tvListaTelefono.text = telefono

                        Picasso.get()
                                // .load(R.drawable.user_avatar)
                                .load(Uri.parse(foto))
                                .transform(CirculoTransformacion())
                                .resize(178, 178)
                                .into(imgListaCoche)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })



            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        return lista

    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(coche: Coche) {
        abrirCoche(coche)
    }

    /**
     * Se llama cuando hemos pulsado un coche, abrimos el fragment detalle
     */
    private fun abrirCoche(coche : Coche){
        //Se oculta el floating button

        //Se llama al detalle fragment
        val cocheDetalle = DetalleCocheFragment(coche, listaCochesFiltro, fabBoton, idUbica)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.listaCochesBuscar, cocheDetalle)
        transaction.addToBackStack(null)
        transaction.commit()

    }



    /**
     * Tarea asíncrona para la carga de coches
     */
    inner class TareaCargarCoches : AsyncTask<Void?, Void?, Void?>() {

        // antes de ejecutar
        override fun onPreExecute() {
            //comprobamos el swipe
            if (cochesListaSwipeRefresh.isRefreshing) {
                cochesListaSwipeRefresh.isRefreshing = false

            }
        }
        // Tarea
        override fun doInBackground(vararg args: Void?): Void? {
            try {

                //dependiendo de si venimos del filtro o no, nos iremos a una opción u a otra
                if (listaCochesFiltro.size == 0){
                    listaCoches = rellenarArrayCoche()
                } else {
                    listaCoches = rellenarArrayFiltro()
                }


            } catch (e: Exception) {
            }
            return null
        }
        //después de ejecutar
        override fun onPostExecute(args: Void?) {

            //detecta cuando pulsamos en un item
            cocheAdapter = CocheListAdapter(listaCoches) {
                eventoClicFila(it)
            }

            cochesListaRecycler.adapter = cocheAdapter
            // Avismos que ha cambiado
            cocheAdapter.notifyDataSetChanged()
            cochesListaRecycler.setHasFixedSize(true)

            Toast.makeText(context, "Tus coches cargados", Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Si paramos cancelamos la tarea
     */
    override fun onStop() {
        super.onStop()
        tareaCoche.cancel(true)
    }




    private fun visualizarListaItems() {
        cocheAdapter = CocheListAdapter(listaCoches) {
            eventoClicFila(it)
        }

        cochesListaRecycler.adapter = cocheAdapter

    }

}