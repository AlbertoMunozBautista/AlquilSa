package com.sheilalberto.alquilsa.ui.buscar

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Ubicacion
import com.squareup.picasso.Picasso

class BuscarFragment( var listaIds: MutableList<String> = mutableListOf<String>(), var listaIdsCoches: MutableList<String> = mutableListOf<String>() ) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private lateinit var fabBuscarFiltro : FloatingActionButton

    //Variables del mapa
    private lateinit var mMap: GoogleMap
    private var mPosicion: FusedLocationProviderClient? = null
    private var localizacion: Location? = null
    private var posicion: LatLng? = null
    private var PERMISOS: Boolean = true

    //Variables de la base de datos
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseUsuarioReference: DatabaseReference

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_buscar, container, false)

        fabBuscarFiltro = root.findViewById(R.id.fabBuscarFiltro)
        leerPoscionGPSActual()
        initMapa()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Botón para acceder al filtro
        fabBuscarFiltro.setOnClickListener{

            fabBuscarFiltro.hide()
            val filtro = FiltroFragment(fabBuscarFiltro)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.buscar_layout, filtro)
            transaction.addToBackStack(null)
            transaction.commit()

        }



    }

    /**
     * Cogemos nuestra posición actual
     */
    private fun leerPoscionGPSActual() {
        mPosicion = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    /**
     * Inicializamos el mapa
     */
    private fun initMapa() {
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.mapaBuscarMapa) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    /**
     * Cargamos el mapa
     */
    @SuppressLint("MissingPermission")
    private fun cargarMapa() {
        Log.i("Mapa", "Configurando Modo Insertar")
        if (this.PERMISOS) {
            mMap.isMyLocationEnabled = true
        }
        cargarMarcadores()
        obtenerPosicion()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        cargarMapa()
    }

    /**
     * Se rescata la posicion del las ubicaciones, se guarda en una lista y se cargan en el mapa
     */
    private fun cargarMarcadores() {

        var myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_negro);
        //listaUbicaciones contiene todas las ubicaciones que nostros queremos que se muestren

        var listaUbicaciones = mutableListOf<Ubicacion>() //Lista de ubicaciones

        db = FirebaseDatabase.getInstance("https://alquilsa-default-rtdb.europe-west1.firebasedatabase.app/")


        //recogemos las referencias a las tablas ubicaciones y usuarios
        databaseReference = db.reference.child("ubicaciones")
        databaseUsuarioReference = db.reference.child("usuarios")


        Log.e("BUSCAR", listaIds.size.toString())

        //recogemos todas las ubicaciones del usuario
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.e("LISTAID", listaIds.size.toString())
                //si la lista no está vacía (viene del filtro) ejecutamos esto
                if(listaIds.isNotEmpty()){
                    snapshot.children.forEach {
                        for (i in 0..listaIds.size -1) {

                            Log.e("BUSCAR", i.toString())
                            Log.e("BUSCAR", it.child("idUbicacion").getValue().toString())

                            if (listaIds[i].toString().equals(it.child("idUbicacion").getValue().toString())) {

                                var nombre = it.child("nombre").getValue().toString()
                                var latitud = it.child("latitud").getValue().toString()
                                var longitud = it.child("longitud").getValue().toString()
                                var idU = it.child("idUsuario").getValue().toString()
                                var idUbicacion = it.child("idUbicacion").getValue().toString()

                                val u = Ubicacion(idUbicacion, idU, nombre, latitud, longitud)


                                listaUbicaciones.add(u)

                            }

                        }

                    }
                } else {

                    //si la lista está vacía (no viene del filtro)
                    snapshot.children.forEach {

                        var nombre = it.child("nombre").getValue().toString()
                        var latitud = it.child("latitud").getValue().toString()
                        var longitud = it.child("longitud").getValue().toString()
                        var idU = it.child("idUsuario").getValue().toString()
                        var idUbicacion = it.child("idUbicacion").getValue().toString()

                        val u = Ubicacion(idUbicacion, idU, nombre, latitud, longitud)


                        listaUbicaciones.add(u)


                    }
                }

                //futura posible implementación (se añade foto al icono del mapa)
                databaseUsuarioReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            for (u in 0..listaUbicaciones.size - 1) {
                                if (it.child("idUsuario").getValue().toString() == listaUbicaciones[u].idUsuario) {
                                    var fotoUsuario = it.child("foto").getValue().toString()


                                    Picasso.get()
                                            .load(fotoUsuario)
                                            .into(object : com.squareup.picasso.Target {

                                                override fun onBitmapLoaded(
                                                        bitmap: Bitmap,
                                                        from: Picasso.LoadedFrom
                                                ) {
                                                    /* Save the bitmap or do something with it here */
                                                    myBitmap = bitmap
                                                    // Set it in the ImageView
                                                    //theView.setImageBitmap(bitmap)
                                                }

                                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                                                override fun onBitmapFailed(
                                                        e: Exception?,
                                                        errorDrawable: Drawable?
                                                ) {
                                                }

                                            })


                                    val estacion = LatLng(
                                            listaUbicaciones[u].latitud.toDouble(),
                                            listaUbicaciones[u].longitud.toDouble()
                                    )


                                    mMap.addMarker(
                                            MarkerOptions()
                                                    .position(estacion)  // Posición
                                                    .title(listaUbicaciones[u].nombre)// Título
                                                    //.snippet(listaSitios[u].estrellas.toString() + " Estrellas") // Subtitulo
                                                    //.icon(BitmapDescriptorFactory.fromBitmap(crearPin(myBitmap!!))
                                                    //))
                                                    .icon(
                                                            BitmapDescriptorFactory.defaultMarker(
                                                                    BitmapDescriptorFactory.HUE_CYAN
                                                            )
                                                    )
                                    )

                                }
                            }
                        }


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


    }


    /**
     * Configuración del mapa
     */
    private fun configurarIUMapa() {
        Log.i("Mapa", "Configurando IU Mapa")
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val uiSettings: UiSettings = mMap.uiSettings
        // Activamos los gestos
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        // Activamos la brújula
        uiSettings.isCompassEnabled = true
        // Activamos los controles de zoom
        uiSettings.isZoomControlsEnabled = true
        // Activamos la barra de herramientas
        uiSettings.isMapToolbarEnabled = true
        // Hacemos el zoom por defecto mínimo
        mMap.setMinZoomPreference(12.0f)
        mMap.setOnMarkerClickListener(this)
    }

    /**
     * Obtiene la posición actual para pasarsela al mapa y que se cargue en nuestra posición
     */
    private fun obtenerPosicion() {
        try {
            //Si tenemos permiso cogemos la localización
            if (this.PERMISOS) {
                val local: Task<Location> = mPosicion!!.lastLocation
                local.addOnCompleteListener(
                        requireActivity()
                ) { task ->
                    if (task.isSuccessful) {
                        localizacion = task.result
                        posicion = LatLng(
                                localizacion!!.latitude,
                                localizacion!!.longitude
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                    } else {
                        Log.i("GPS", "No se encuetra la última posición.")
                    }
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                    requireView(),
                    "No se ha encontrado su posoción actual o el GPS está desactivado",
                    Snackbar.LENGTH_LONG
            ).show();
            Log.e("Exception: %s", e.message.toString())
        }
    }

    /**
     * Iniciamos permisos
     */
    private fun initPermisos() {
        this.PERMISOS = true
    }

    /**
     * Cuando hacemos clic en el mapa
     */
    override fun onMarkerClick(p0: Marker?): Boolean {

        fabBuscarFiltro.hide()

        db = FirebaseDatabase.getInstance("https://alquilsa-default-rtdb.europe-west1.firebasedatabase.app/")
        databaseReference = db.reference.child("ubicaciones")

        var latitud = p0?.position?.latitude.toString()
        var longitud = p0?.position?.longitude.toString()

        Log.e("CONSULTA", p0?.position?.latitude.toString())
        Log.e("CONSULTA", p0?.position?.longitude.toString())

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {

                    if (it.child("latitud").getValue().toString().equals(latitud) && it.child("longitud").getValue().toString().equals(longitud)) {
                        var idUbicacion = it.child("idUbicacion").getValue().toString()

                        Log.e("CONSULTA", idUbicacion)


                        val editarFragment = ListaCochesFragment(fabBuscarFiltro, idUbicacion, listaIdsCoches)
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        transaction.replace(R.id.buscar_layout, editarFragment)
                        transaction.addToBackStack(null)
                        transaction.commit()

                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })





        return false
    }

    //Creamos un pin con la imagen del sitio que estamos visualizando
    private fun crearPin(imagenID: Bitmap): Bitmap? {
        val fotografia = imagenID
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(dp(62f), dp(76f), Bitmap.Config.ARGB_8888)
            result.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(result)
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.map_pin)
            drawable?.setBounds(0, 0, dp(62f), dp(76f))
            drawable?.draw(canvas)
            val roundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val bitmapRect = RectF()
            canvas.save()
            val bitmap = imagenID
            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (bitmap != null) {
                val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val matrix = Matrix()
                val scale = dp(52f) / bitmap.width.toFloat()
                matrix.postTranslate(dp(5f).toFloat(), dp(5f).toFloat())
                matrix.postScale(scale, scale)
                roundPaint.shader = shader
                shader.setLocalMatrix(matrix)
                bitmapRect[dp(5f).toFloat(), dp(5f).toFloat(), dp(52f + 5).toFloat()] = dp(52f + 5).toFloat()
                canvas.drawRoundRect(bitmapRect, dp(26f).toFloat(), dp(26f).toFloat(), roundPaint)
            }
            canvas.restore()
            try {
                canvas.setBitmap(null)
            } catch (e: Exception) {
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return result
    }


    // Densidad de pantalla
    fun dp(value: Float): Int {
        return if (value == 0f) {
            0
        } else
            Math.ceil((resources.displayMetrics.density * value).toDouble()).toInt()
    }


}