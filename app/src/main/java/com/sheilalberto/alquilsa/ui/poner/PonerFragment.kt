package com.sheilalberto.alquilsa.ui.poner

import android.content.Context
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Coche
import kotlinx.android.synthetic.main.fragment_poner.*

class PonerFragment : Fragment() {

    private lateinit var fabPonerAnadir: FloatingActionButton

    private var listaCoches = mutableListOf<Coche>() //Lista de coches
    private lateinit var cocheAdapter: CocheListAdapter //Adaptador de coches
    private lateinit var tareaCoche: PonerFragment.TareaCargarCoches // Tarea hilo para cargar coches
    private var paintSweep = Paint()
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    private var idUsuario = ""

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_poner, container, false)

        fabPonerAnadir = root.findViewById(R.id.fabPonerAnadir)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cocheRecycler.layoutManager = LinearLayoutManager(context)

        //Cuando hacemos click en el fab button nos lleva al fragment de añadir coches
        fabPonerAnadir.setOnClickListener {

            fabPonerAnadir.hide()
            //Cargamos el fragment de añadir coches
            val anadirCoche = AnadirCocheFragment(fabPonerAnadir)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.poner_layout, anadirCoche)
            transaction.addToBackStack(null)
            transaction.commit()

        }

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

        cocheSwipeRefresh.setColorSchemeResources(R.color.principal_oscuro)
        cocheSwipeRefresh.setProgressBackgroundColorSchemeResource(R.color.secundario)
        cocheSwipeRefresh.setOnRefreshListener {

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
     * Rellenamos la lista con los coches que posteriormente cargaremos en el recycler
     */
    private fun rellenarArrayCoche() : MutableList<Coche> {

        var lista = mutableListOf<Coche>() //Lista de coches

        db = FirebaseDatabase.getInstance("https://alquilsaguarro-default-rtdb.europe-west1.firebasedatabase.app/")

        val prefs = requireActivity().getSharedPreferences(
                getString(R.string.prefs_file),
                Context.MODE_PRIVATE
        )

        //Recogemos el id del usuario qu ha iniciado sesión
        idUsuario = prefs?.getString("idUsuario", "null").toString()
        Log.e("NOMBRE", "ID" + idUsuario.toString())

        //recogemoe la referencia a la tabla coches
        databaseReference = db.reference.child("coches")

        //Consulta que almacena en una lista los coches que pertenecen al usuario que actualmente está activo
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {

                    if (it.child("idUsuario").getValue().toString() == idUsuario) {
                        val nombre = it.child("nombre").getValue().toString()
                        val asientos = it.child("asientos").getValue().toString()
                        val autonomia = it.child("autonomia").getValue().toString()
                        val combustible = it.child("combustible").getValue().toString()
                        val disponible = it.child("disponible").getValue().toString().toBoolean()
                        val foto = it.child("foto").getValue().toString()
                        val idCoche = it.child("idCoche").getValue().toString()
                        val idUbicacion = it.child("idUbicacion").getValue().toString()
                        val idU = it.child("idU").getValue().toString()
                        val likes = it.child("likes").getValue().toString().toInt()
                        val matricula = it.child("matricula").getValue().toString()
                        val precio = it.child("precio").getValue().toString().toFloat()
                        val tipo = it.child("tipo").getValue().toString()
                        val transmision = it.child("transmision").getValue().toString()

                        val c = Coche(idCoche, matricula, idU, foto, nombre, likes, idUbicacion,
                                autonomia, combustible, transmision, asientos, tipo,
                                precio, disponible)

                        lista.add(c)
                    }
                }


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
        //abrirCoche(coche)
    }



    /**
     * Tarea asíncrona para la carga de coches
     */
    inner class TareaCargarCoches : AsyncTask<Void?, Void?, Void?>() {

        // antes de ejecutar
        override fun onPreExecute() {
            //comprobamos el swipe
            if (cocheSwipeRefresh.isRefreshing) {
                cocheSwipeRefresh.isRefreshing = false

            }
        }
        // Tarea
        override fun doInBackground(vararg args: Void?): Void? {
            try {
                listaCoches = rellenarArrayCoche()

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

            cocheRecycler.adapter = cocheAdapter
            // Avismos que ha cambiado
            cocheAdapter.notifyDataSetChanged()
            cocheRecycler.setHasFixedSize(true)

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

        cocheRecycler.adapter = cocheAdapter

    }

}

