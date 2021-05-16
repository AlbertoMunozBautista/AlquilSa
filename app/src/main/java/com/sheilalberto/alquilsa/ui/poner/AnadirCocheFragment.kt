package com.sheilalberto.alquilsa.ui.poner

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sheilalberto.alquilsa.R
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.sheilalberto.alquilsa.CirculoTransformacion
import com.sheilalberto.alquilsa.clases.Coche
import com.sheilalberto.alquilsa.clases.Ubicacion
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_anadir_coche.*
import java.io.IOException
import java.util.*

class AnadirCocheFragment(
        private val fabPonerAnadir: FloatingActionButton
) : Fragment(){

    //Variables para los botones
    private var electrico = false
    private var gasolina = false
    private var diesel = false
    private var automatica = false
    private var manual = false
    private var asiento1 = false
    private var asiento2 = false
    private var asiento3 = false
    private var asiento4 = false
    private var asiento5 = false
    private var asiento5Mas = false
    private var moto = false
    private var coche = false
    private var furgoneta = false
    private var deportivo = false

    //variables al almacenamiento, base de datos y autenticación.
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseUbicacionReference: DatabaseReference
    private lateinit var databaseCocheReference: DatabaseReference
    private lateinit var Storage: FirebaseStorage

    //Variable de la cámara
    private val GALERIA = 1
    private val CAMARA = 2
    var fotoUri: Uri? = null

    private var idUsuario = ""
    private var idCoche = ""
    private var idUbicacion = ""
    private var matricula = ""
    private var foto = ""
    private var nombre = ""
    private var likes = 0
    private var autonomia = ""
    private var combustible = ""
    private var transmision = ""
    private var asientos = ""
    private var tipo = ""
    private var precio = ""
    private var disponible = true

    private var index = 0
    private var listaUbicaciones = mutableListOf<Ubicacion>()
    private var listaUbicacionesNombres = mutableListOf<String>()
    private var listaUbicacionesId = mutableListOf<String>()
    var lista = mutableListOf<Ubicacion>()


    private lateinit var spinnerAnadirCoche : Spinner

    /**
     * En el onCreate rellenamos el array que vamos a usar para cargar el spinner
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rellenarArrayUbicacion()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_anadir_coche, container, false)

        val btnAnadirCancelar : Button = root.findViewById(R.id.btnAnadirCancelar)
        val btnAnadirAnadir : Button = root.findViewById(R.id.btnAnadirAnadir)
        val btnAnadirElectrico : Button = root.findViewById(R.id.btnAnadirElectrico)
        val btnAnadirGasolina : Button = root.findViewById(R.id.btnAnadirGasolina)
        val btnAnadirDiesel : Button = root.findViewById(R.id.btnAnadirDiesel)
        val btnAnadirAutomatica : Button = root.findViewById(R.id.btnAnadirAutomatica)
        val btnAnadirManual : Button = root.findViewById(R.id.btnAnadirManual)
        val btnAnadirAsiento1 : Button = root.findViewById(R.id.btnAnadirAsiento1)
        val btnAnadirAsiento2 : Button = root.findViewById(R.id.btnAnadirAsiento2)
        val btnAnadirAsiento3 : Button = root.findViewById(R.id.btnAnadirAsiento3)
        val btnAnadirAsiento4 : Button = root.findViewById(R.id.btnAnadirAsiento4)
        val btnAnadirAsiento5 : Button = root.findViewById(R.id.btnAnadirAsiento5)
        val btnAnadirAsiento5Mas : Button = root.findViewById(R.id.btnAnadirAsiento5Mas)
        val btnAnadirMoto : Button = root.findViewById(R.id.btnAnadirMoto)
        val btnAnadirCoche : Button = root.findViewById(R.id.btnAnadirCoche)
        val btnAnadirFurgoneta : Button = root.findViewById(R.id.btnAnadirFurgoneta)
        val btnAnadirDeportivo : Button = root.findViewById(R.id.btnAnadirDeportivo)
        val pbAnadirAutonomia : SeekBar = root.findViewById(R.id.pbAnadirAutonomia)
        val tvAnadirAutonomia : TextView = root.findViewById(R.id.tvAnadirAutonomia)
        val etAnadirPrecio : EditText = root.findViewById(R.id.etAnadirPrecio)
        val etAnadirMatricula : EditText = root.findViewById(R.id.etAnadirMatricula)
        val etAnadirNombre : EditText = root.findViewById(R.id.etAnadirNombre)
        val imaAnadirFoto : ImageView = root.findViewById(R.id.imaAnadirFoto)
        spinnerAnadirCoche = root.findViewById(R.id.spinnerAnadirUbicacion)

        var dialog = Dialog(requireActivity())
        Storage = FirebaseStorage.getInstance()

        val prefs = requireActivity().getSharedPreferences(
                getString(R.string.prefs_file),
                Context.MODE_PRIVATE
        )

        //Guardamos el usuario que está activo
        idUsuario = prefs?.getString("idUsuario", "null").toString()
        Log.e("ARRAY", "ID" + idUsuario.toString())

        Log.e("ARRAY", "ARRAY" + listaUbicaciones.size.toString())

        //Inicializamos el progress bar a 0, le decimos que su máximo va a ser 100 y cnd cambie su valor lo vamos cambiando
        //en su tv sincronizado
        pbAnadirAutonomia.progress = 0
        pbAnadirAutonomia.max = 1000
        pbAnadirAutonomia.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                            seekBar: SeekBar, progress: Int, fromUser: Boolean
                    ) {
                        tvAnadirAutonomia.setText("$progress")
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}


                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })

        //Cuando pusalmos cancelar volvemos al poner fragment
        btnAnadirCancelar.setOnClickListener{

            fabPonerAnadir.show()
            val ponerfrag = PonerFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.anadir_layout, ponerfrag)
            transaction.addToBackStack(null)
            transaction.commit()

        }

        //Cuando le damos a añadir llamamos a la funcion de insertar el coche
        btnAnadirAnadir.setOnClickListener{

            insertarCoche()

        }


        //Cuando pulsamos en la foto
        imaAnadirFoto.setOnClickListener{
            //Abrimos un dialog con las 2 opciones (camara o galeria)
            dialog.setContentView(R.layout.camara_layout)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            //Se rescatan las imágenes del layout de la cámara (si no se rescatan no funciona)
            var imaCamaraFoto: ImageView = dialog.findViewById(R.id.imaCamaraFoto)
            var imaCamaraGaleria: ImageView = dialog.findViewById(R.id.imaCamaraGaleria)
            var tvCamaraFoto: TextView = dialog.findViewById(R.id.tvCamaraFoto)
            var tvCamaraGaleria: TextView = dialog.findViewById(R.id.tvCamaraGaleria)

            imaCamaraFoto.setOnClickListener(){
                fotoCamara()
                dialog.dismiss()
            }
            imaCamaraGaleria.setOnClickListener(){
                fotoGaleria()
                dialog.dismiss()
            }
            tvCamaraFoto.setOnClickListener(){
                fotoCamara()
                dialog.dismiss()
            }
            tvCamaraGaleria.setOnClickListener(){
                fotoGaleria()
                dialog.dismiss()
            }

            dialog.show()
        }


        //Cuando se pulsa en los distintos botones del layout, vamos cambiando su valor y su color de fondo
        btnAnadirGasolina.setOnClickListener{
            combustible()
            btnAnadirGasolina.setBackgroundResource(R.drawable.boton_redondo)
            gasolina = true
        }

        btnAnadirElectrico.setOnClickListener{
            combustible()
            btnAnadirElectrico.setBackgroundResource(R.drawable.boton_redondo)
            electrico = true
        }

        btnAnadirDiesel.setOnClickListener{
            combustible()
            btnAnadirDiesel.setBackgroundResource(R.drawable.boton_redondo)
            diesel = true
        }

        btnAnadirAutomatica.setOnClickListener{
            transmision()
            btnAnadirAutomatica.setBackgroundResource(R.drawable.boton_redondo)
            automatica = true
        }

        btnAnadirManual.setOnClickListener{
            transmision()
            btnAnadirManual.setBackgroundResource(R.drawable.boton_redondo)
            manual = true
        }

        btnAnadirAsiento1.setOnClickListener{
            asientos()
            btnAnadirAsiento1.setBackgroundResource(R.drawable.boton_redondo)
            asiento1 = true
        }

        btnAnadirAsiento2.setOnClickListener{
            asientos()
            btnAnadirAsiento2.setBackgroundResource(R.drawable.boton_redondo)
            asiento2 = true
        }

        btnAnadirAsiento3.setOnClickListener{
            asientos()
            btnAnadirAsiento3.setBackgroundResource(R.drawable.boton_redondo)
            asiento3 = true
        }

        btnAnadirAsiento4.setOnClickListener{
            asientos()
            btnAnadirAsiento4.setBackgroundResource(R.drawable.boton_redondo)
            asiento4 = true
        }

        btnAnadirAsiento5.setOnClickListener{
            asientos()
            btnAnadirAsiento5.setBackgroundResource(R.drawable.boton_redondo)
            asiento5 = true
        }
        btnAnadirAsiento5Mas.setOnClickListener{
            asientos()
            btnAnadirAsiento5Mas.setBackgroundResource(R.drawable.boton_redondo)
            asiento5Mas = true
        }

        btnAnadirMoto.setOnClickListener{
            tipo()
            btnAnadirMoto.setBackgroundResource(R.drawable.boton_redondo)
            moto = true
        }

        btnAnadirCoche.setOnClickListener{
            tipo()
            btnAnadirCoche.setBackgroundResource(R.drawable.boton_redondo)
            coche = true
        }

        btnAnadirFurgoneta.setOnClickListener{
            tipo()
            btnAnadirFurgoneta.setBackgroundResource(R.drawable.boton_redondo)
            furgoneta = true
        }

        btnAnadirDeportivo.setOnClickListener{
            tipo()
            btnAnadirDeportivo.setBackgroundResource(R.drawable.boton_redondo)
            deportivo = true
        }


        return root
    }

    /**
     * Función que recoge todos los valores introducidos en el layout y si son válidos los inserta
     * en la base de datos como un nuevo coche
     */
    private fun insertarCoche() {

        //referencia a la tabla coches
        databaseCocheReference = db.reference.child("coches")

        //recogemos los diferentes valores de los campos
        combustible = obtenerCombustible()
        transmision = obtenerTransmision()
        asientos = obtenerAsientos()
        tipo = obtenerTipo()
        Log.e("precio", "precio anteesss " +etAnadirPrecio?.text.toString())
        precio = etAnadirPrecio.text.toString()
        matricula = etAnadirMatricula.text.toString()
        autonomia =  tvAnadirAutonomia.text.toString()
        likes = 0
        disponible = true
        nombre = etAnadirNombre.text.toString()

        index = spinnerAnadirCoche.selectedItemPosition
        idUbicacion = listaUbicacionesId.get(index)

        //comprobamos que son válidos
        idCoche = UUID.randomUUID().toString()
        if(nombre.isEmpty()){
            Toast.makeText(requireActivity(), "Compruebe de nuevo los datos introducidos", Toast.LENGTH_SHORT).show()
            etAnadirNombre.requestFocus()
            etAnadirNombre.setError("El nombre no puede estar vacío")
        } else if (matricula.isEmpty()){
            Toast.makeText(requireActivity(), "Compruebe de nuevo los datos introducidos", Toast.LENGTH_SHORT).show()
            etAnadirMatricula.requestFocus()
            etAnadirMatricula.setError("Introduce una matricula")
        }
        else if(precio.isEmpty()){
            Toast.makeText(requireActivity(), "Compruebe de nuevo los datos introducidos", Toast.LENGTH_SHORT).show()
            etAnadirPrecio.requestFocus()
            etAnadirPrecio.setError("Introduce un precio")
        }
        else if(!gasolina && !electrico && !diesel){
            Toast.makeText(requireActivity(), "Es obligatorio seleccionar un tipo de combustible", Toast.LENGTH_SHORT).show()
        }
        else if(!automatica &&  !manual){
            Toast.makeText(requireActivity(), "Es obligatorio seleccionar un tipo de transmisión", Toast.LENGTH_SHORT).show()
        }
        else if(!asiento1 && !asiento2 && !asiento3 && !asiento4  && !asiento5  && !asiento5Mas){
            Toast.makeText(requireActivity(), "Es obligatorio seleccionar número de asientos", Toast.LENGTH_SHORT).show()
        }
        else if(!coche && !moto && !furgoneta && !deportivo){
            Toast.makeText(requireActivity(), "Es obligatorio seleccionar tipo de coche", Toast.LENGTH_SHORT).show()
        }
        else if (fotoUri == null){
            Toast.makeText(requireActivity(), "Es obligatorio introducir una foto", Toast.LENGTH_SHORT).show()
        }
        else {
            //si está bien hacemos el insert

            //Le damos a la foto un nombre y las almacenamos el el Storage dentro de la carpeta fotoCoches
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/fotosCoches/$filename")

            //almacenamos la foto en el storage
            ref.putFile(fotoUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {

                    foto = it.toString()

                    //creamos el objeto coche
                    val c = Coche(idCoche, matricula, idUsuario, foto,
                            nombre, likes, idUbicacion, autonomia,
                            combustible, transmision, asientos, tipo, precio.toFloat(), disponible)

                    Log.e("precio", "precio" +precio.toString())

                    //lo guardamos en la base de datos
                    databaseCocheReference.child(idCoche).setValue(c)

                    //volvemos al ponerFragment
                    fabPonerAnadir.show()
                    val ponerFrag = PonerFragment()
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    transaction.replace(R.id.anadir_layout, ponerFrag)
                    transaction.addToBackStack(null)
                    transaction.commit()


                }
            }
        }



    }

    /**
     * Función donde obtenemos el valor que va a tener la transmision
     */
    private fun obtenerTransmision(): String {
        if(manual){
            return "Manual"
        } else {
            return "Automático"
        }
    }

    /**
     * Función donde obtenemos el valor que va a tener el combustible
     */
    private fun obtenerCombustible(): String {
        if(electrico){
            return "Eléctrico"
        }else if (gasolina){
            return "Gasolina"
        }else{
            return "Diesel"
        }
    }

    /**
     * Función donde obtenemos el valor que va a tener los asientos
     */
    private fun obtenerAsientos(): String {

        if(asiento1){
            return "1"
        }else if (asiento2){
            return "2"
        }else if (asiento3){
            return "3"
        }else if (asiento4){
            return "4"
        }else if (asiento5){
            return "5"
        }else {
            return "+5"
        }
    }

    /**
     * Función donde obtenemos el valor que va a tener el tipo
     */
    private fun obtenerTipo(): String {

        if(moto){
            return "Moto"
        }else if (coche){
            return "Coche"
        }else if (furgoneta){
            return "Furgoneta"
        }else {
            return "Deportivo"
        }
    }

    /**
     * Cuando seleccionamos un botón de combustible reseteamos los demás
     */
    private fun combustible() {

        btnAnadirGasolina.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirDiesel.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirElectrico.setBackgroundResource(R.drawable.boton_fondo_transparente)

        gasolina = false
        diesel = false
        electrico = false
    }

    /**
     * Cuando seleccionamos un botón de transmision reseteamos los demás
     */
    private fun transmision() {

        btnAnadirAutomatica.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirManual.setBackgroundResource(R.drawable.boton_fondo_transparente)

        automatica = false
        manual = false
    }

    /**
     * Cuando seleccionamos un botón de asientos reseteamos los demás
     */
    private fun asientos() {

        btnAnadirAsiento1.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirAsiento2.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirAsiento3.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirAsiento4.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirAsiento5.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirAsiento5Mas.setBackgroundResource(R.drawable.boton_fondo_transparente)

        asiento1 = false
        asiento2 = false
        asiento3 = false
        asiento4 = false
        asiento5Mas = false

    }

    /**
     * Cuando seleccionamos un botón de tipo reseteamos los demás
     */
    private fun tipo() {

        btnAnadirMoto.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirCoche.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirFurgoneta.setBackgroundResource(R.drawable.boton_fondo_transparente)
        btnAnadirDeportivo.setBackgroundResource(R.drawable.boton_fondo_transparente)

        moto = false
        coche = false
        furgoneta = false
        deportivo = false

    }

    /**
     * Función que va a rellenar un spinner con las ubicaciones de el usuario activo
     */
    private fun rellenarArrayUbicacion(){

        var nombre = ""
        var latitud = ""
        var longitud = ""
        var idU = ""
        var idUbicacion = ""

        var pruebecita = mutableListOf<String>() //Lista de sitios
        pruebecita.add("eee")
        pruebecita.add("ii")

        db = FirebaseDatabase.getInstance("https://alquilsaguarro-default-rtdb.europe-west1.firebasedatabase.app/")

        //recogemos la referencia a la tabla ubicaciones
        databaseUbicacionReference = db.reference.child("ubicaciones")

        //recogemos todas las ubicaciones que tiene el usuario activo
        databaseUbicacionReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    Log.e("ARRAY", "JODAAAAAAAA")
                }
                snapshot.children.forEach {

                    if (it.child("idUsuario").getValue().toString() == idUsuario) {
                        Log.e("ARRAY", "JODEEEEEEEEEE")
                        nombre = it.child("nombre").getValue().toString()
                        latitud = it.child("latitud").getValue().toString()
                        longitud = it.child("longitud").getValue().toString()
                        idU = it.child("idUsuario").getValue().toString()
                        idUbicacion = it.child("idUbicacion").getValue().toString()

                        val u = Ubicacion(idUbicacion, idU, nombre, latitud, longitud)
                        Log.e("ARRAY", u.nombre)

                        Log.e("ARRAY", "LISTA TAMAÑO" + lista.size.toString())

                        listaUbicacionesNombres.add(u.nombre)
                        listaUbicacionesId.add(u.idUbicacion)


                    }

                }

                Log.e("ARRAY", "LISTATA" + pruebecita.size.toString())
                Log.e("ARRAY", "LISTITA" + lista.size.toString())
                Log.e("ARRAY", "LISTOTA" + listaUbicaciones.size.toString())
                Log.e("ARRAY", "LISTOTA" + listaUbicacionesNombres.size.toString())

                //las añadimos en el spinner
                spinnerAnadirCoche.setAdapter(
                        ArrayAdapter<String>(
                                requireActivity(),
                                android.R.layout.simple_spinner_dropdown_item,
                                listaUbicacionesNombres
                        )
                )


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        Log.e("ARRAY", "LISTA" + lista.size.toString())
        Log.e("ARRAY", "HOSTIA PUTA" + listaUbicacionesNombres.size.toString())



    }

    fun fotoGaleria() {
        val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(
                galleryIntent,
                GALERIA
        )
    }

    private fun fotoCamara() {
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "Imagen")
        fotoUri = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)!!
        val camaraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        camaraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
        startActivityForResult(camaraIntent, CAMARA)
    }


    /**
     * Según la opción pondremos en la imagen del layout la foto de la cámara o de la galería
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("FOTO", "Opción::--->$requestCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == GALERIA) {
            Log.d("FOTO", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                fotoUri = data.data
                try {
                    Picasso.get().load(fotoUri).transform(CirculoTransformacion()).into(imaAnadirFoto)
                } catch (e: IOException) {
                    e.printStackTrace()
                    //Toast.makeText(this, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (requestCode == CAMARA) {

            Picasso.get().load(fotoUri).transform(CirculoTransformacion()).into(imaAnadirFoto)

        }
    }







}