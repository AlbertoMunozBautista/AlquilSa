package com.sheilalberto.alquilsa.ui.perfil

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.sheilalberto.alquilsa.CirculoTransformacion
import com.sheilalberto.alquilsa.MainActivity
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil.*
import java.io.IOException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class PerfilFragment : Fragment() {

    //Creacion de variables
    private var idUsuario = ""
    private var nombre = ""
    private var email = ""
    private var contra = ""
    private var dni = ""
    private var telefono = ""
    private var diaFecha = ""
    private var mesFecha = ""
    private var anoFecha = ""
    private var foto = ""

    private lateinit var imaPerfilFoto : ImageView
    private lateinit var etPerfilNombre : EditText
    private lateinit var etPerfilEmail : EditText
    private lateinit var etPerfilContra : EditText
    private lateinit var etPerfilDni : EditText
    private lateinit var etPerfilTelefono : EditText
    private lateinit var etPerfilFecha : DatePicker
    private lateinit var btnPerfilGuardar : Button
    private lateinit var btnPerfilCancelar : Button


    private var google = false

    //Variables utilizadas para las bases de datos de Firebase
    private lateinit var Auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var Storage: FirebaseStorage

    private val GALERIA = 1
    private val CAMARA = 2
    var fotoUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_perfil, container, false)


        //Recogemos los componentes del layout en las variables
        imaPerfilFoto = root.findViewById(R.id.imaPerfilFoto)
        etPerfilNombre = root.findViewById(R.id.etPerfilNombre)
        etPerfilEmail = root.findViewById(R.id.etPerfilEmail)
        etPerfilContra = root.findViewById(R.id.etPerfilContra)
        etPerfilDni = root.findViewById(R.id.etPerfilDni)
        etPerfilTelefono = root.findViewById(R.id.etPerfilTelefono)
        etPerfilFecha = root.findViewById(R.id.etPerfilFecha)
        btnPerfilGuardar = root.findViewById(R.id.btnPerfilGuardar)
        btnPerfilCancelar = root.findViewById(R.id.btnPerfilCancelar)

        var dialog = Dialog(requireActivity())

        //Instanciamos las bases de datos
        Auth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://alquilsa-default-rtdb.europe-west1.firebasedatabase.app/")
        databaseReference = db.reference.child("usuarios")//tabla usuarios
        Storage = FirebaseStorage.getInstance()//bbdd que usamos para las imagenes

        val prefs = activity?.getSharedPreferences(
            getString(R.string.prefs_file),
            Context.MODE_PRIVATE
        )

        idUsuario = prefs?.getString("idUsuario", "null").toString()

        //Cargamos los datos del usuario
        cargarDatos()

        /*
        Si pinchamos en el botón cancelar, nos lleva al fragment de antes, en este caso el Main
         */
        btnPerfilCancelar.setOnClickListener{
            entrarMain()
        }

        /*
        Si pulsamos el boton guardar, recogemos la información que tengan los campos del layput
        y los mandamos al metodo comprobarCampos() para asegurarnos de que no estén vacios
         */
        btnPerfilGuardar.setOnClickListener{
            nombre = etPerfilNombre.text.toString()
            email = etPerfilEmail.text.toString()
            contra = etPerfilContra.text.toString()
            dni = etPerfilDni.text.toString()
            telefono = etPerfilTelefono.text.toString()
            comprobarCampos(nombre, email, contra, dni, telefono)

        }

        /*
        Si pulsamos la imagen
         */
        imaPerfilFoto.setOnClickListener{ view ->

            if(!google){
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

        }


        return root
    }

    /*
    Método que comprueba que los campos del layout no estén vaíos (los que recibe)
     */
    private fun comprobarCampos(nombre: String, email:String, contra: String, dni: String, telefono: String) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed)
        )
        val colors = intArrayOf(
            Color.RED,
            Color.RED,
            Color.RED,
            Color.RED
        )

        val myList = ColorStateList(states, colors)

        val statesBlack = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed)
        )

        val colorsBlack = intArrayOf(
            Color.BLACK,
            Color.BLACK,
            Color.BLACK,
            Color.BLACK
        )

        val myListBlack = ColorStateList(statesBlack, colorsBlack)

        var registro = true;



        val pattern: Pattern = Pattern
            .compile(
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
            )
        val mather: Matcher = pattern.matcher(email)

        if(nombre.isEmpty()){
            tiPerfilNombre.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }
            registro=false
            etPerfilNombre.requestFocus()
            tiPerfilNombre.setError("El nombre no puede estar vacío")
        }else{
            tiPerfilNombre.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etPerfilNombre.requestFocus()
            tiPerfilNombre.setError(null)
        }
        if(!mather.find()){
            tiPerfilEmail.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }

            registro=false
            etPerfilEmail.requestFocus()
            tiPerfilEmail.setError("Email: debe tener un formato adecuado")
        }else{
            tiPerfilEmail.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etPerfilEmail.requestFocus()
            tiPerfilEmail.setError(null)
        }
        if(contra.length < 5){
            tiPerfilContra.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }

            registro=false
            etPerfilContra.requestFocus()
            tiPerfilContra.setError("La contraseña debe tener más de 6 carácteres")
        }else{
            tiPerfilContra.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etPerfilContra.requestFocus()
            tiPerfilContra.setError(null)
        }
        if(dni.isEmpty()){
            tiPerfilDni.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }

            registro=false
            etPerfilDni.requestFocus()
            tiPerfilDni.setError("El DNI no puede estar vacío")
        }else{
            tiPerfilDni.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etPerfilDni.requestFocus()
            tiPerfilDni.setError(null)
        }
        if(telefono.isEmpty()){
            tiPerfilTelefono.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }

            registro=false
            etPerfilTelefono.requestFocus()
            tiPerfilTelefono.setError("El telefono no puede estar vacío")
        }else{
            tiPerfilTelefono.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etPerfilTelefono.requestFocus()
            tiPerfilTelefono.setError(null)
        }

        if(registro){
            editarDatos()
        }
    }

    /*
    Método que guardaría cambios en la bbdd en caso de que se hayan editados algunos campos del perfil
     */
    private fun editarDatos() {

        val user = Auth.currentUser
        //Actualizamos el email y la contraseña del usuario
        user!!.updateEmail(etPerfilEmail.text.toString())
        user!!.updatePassword(etPerfilContra.text.toString())

        //Si no hemos introducido ninguna foto, creamos un usuario con los datos recogidos
        if (fotoUri == null){
            val u = Usuario(idUsuario, etPerfilNombre.text.toString(), etPerfilEmail.text.toString(), etPerfilContra.text.toString(),
                foto, etPerfilDni.text.toString(), etPerfilFecha.dayOfMonth.toString().toInt(), etPerfilFecha.month.toString().toInt(),
                etPerfilFecha.year.toString().toInt(), etPerfilTelefono.text.toString(), google)

            databaseReference.setValue(u)

        } else {
            //Creamos un nombre aleatorio para la imagen, y le indicamos la ruta en la que se va a guardar
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/fotosUsuarios/$filename")

            ref.putFile(fotoUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {

                    foto = it.toString()
                    //Creamos un nuevo usuario y lo guardamos en la base de datos
                    val u = Usuario(idUsuario, etPerfilNombre.text.toString(), etPerfilEmail.text.toString(), etPerfilContra.text.toString(),
                        foto, dni, etPerfilFecha.dayOfMonth.toString().toInt(), etPerfilFecha.month.toString().toInt(),
                        etPerfilFecha.year.toString().toInt(), etPerfilTelefono.text.toString(), google)

                    databaseReference.setValue(u)

                }
            }
        }

        entrarMain()


    }

    /*
    Metodo que nos va a cargar los datos que haya guardados en la base en los campos del layut de perfil
     */
    private fun cargarDatos() {

        //Instanciamos la base de datos. Tabla usuarios, campo idUsuario
        databaseReference = db.reference.child("usuarios").child(idUsuario)

        var getData = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //Guardamos en las variables los datos que tenga ese idUsuario en concreto (con el que nos hemos logueado)
                nombre = snapshot.child("nombre").getValue().toString()
                email = snapshot.child("email").getValue().toString()
                foto = snapshot.child("foto").getValue().toString()
                dni = snapshot.child("dni").getValue().toString()
                telefono = snapshot.child("telefono").getValue().toString()
                contra = snapshot.child("contra").getValue().toString()
                diaFecha = snapshot.child("diaFecha").getValue().toString()
                mesFecha = snapshot.child("mesFecha").getValue().toString()
                anoFecha = snapshot.child("anoFecha").getValue().toString()
                google = snapshot.child("google").getValue().toString().toBoolean()


                //Si nos hemos logueado con google
                if (google){

                    //No podremos editar los campos email, contraseña y nombre
                    etPerfilEmail.setBackgroundColor(getResources().getColor(R.color.grisPerfil))
                    etPerfilContra.setBackgroundColor(getResources().getColor(R.color.grisPerfil))
                    etPerfilNombre.setBackgroundColor(getResources().getColor(R.color.grisPerfil))

                    etPerfilContra.isEnabled = false
                    etPerfilNombre.isEnabled = false
                    etPerfilEmail.isEnabled = false

                }

                //Recogemos la información de todos los campos
                etPerfilNombre.setText(nombre)
                etPerfilEmail.setText(email)
                etPerfilDni.setText(dni)
                etPerfilTelefono.setText(telefono)
                etPerfilContra.setText(contra)
                etPerfilFecha.init(anoFecha.toString().toInt(), mesFecha.toString().toInt(), diaFecha.toString().toInt(),
                    DatePicker.OnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
                        // Notify the user.
                    })


                Picasso.get()
                    // .load(R.drawable.user_avatar)
                    .load(Uri.parse(foto))
                    .transform(CirculoTransformacion())
                    .resize(178, 178)
                    .into(imaPerfilFoto)

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NOMBRE", error.message.toString())
            }
        }

        databaseReference.addValueEventListener(getData)
        databaseReference.addListenerForSingleValueEvent(getData)



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
                    Picasso.get().load(fotoUri).transform(CirculoTransformacion()).into(imaPerfilFoto)
                } catch (e: IOException) {
                    e.printStackTrace()
                    //Toast.makeText(this, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (requestCode == CAMARA) {

            Picasso.get().load(fotoUri).transform(CirculoTransformacion()).into(imaPerfilFoto)

        }
    }

    /*
    Entramos en el layout principal
     */
    private fun entrarMain(){
        val mainIntent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(mainIntent)
    }


}