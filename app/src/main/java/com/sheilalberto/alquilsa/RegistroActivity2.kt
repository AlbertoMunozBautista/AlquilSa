package com.sheilalberto.alquilsa

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.sheilalberto.alquilsa.clases.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_registro2.*
import java.io.IOException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegistroActivity2 : AppCompatActivity() {

    //Creación de variables
    private var idUsuario = ""
    private var nombre = ""
    private var email = ""
    private var contra = ""
    private var dni = ""
    private var telefono = ""
    private var diaFecha = 0
    private var mesFecha = 0
    private var anoFecha = 0
    private var foto = ""

    private val GALERIA = 1
    private val CAMARA = 2
    var fotoUri: Uri? = null

    private lateinit var Auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var Storage: FirebaseStorage //BBDD donde vamos a almacenar las fotos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro2)

        //Instanciamos las bases de datos
        Auth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://alquilsa-default-rtdb.europe-west1.firebasedatabase.app/")
        databaseReference = db.reference.child("usuarios")//tabla usuarios
        Storage = FirebaseStorage.getInstance()

        var dialog = Dialog(this)

        /*
        Si hacemos click en el boton de aceptar, recogemos la información que hayamos introducido
        en los campos del Registro y los mandamos al método comprobarCampos() para asegurarnos
        a que ningun de ellos quede vacío
         */
        btnRegistro2Aceptar.setOnClickListener{
            nombre = etRegistro2Nombre.text.toString()
            email = etRegistro2Email.text.toString()
            contra = etRegistro2Contra.text.toString()
            dni = etRegistro2Dni.text.toString()
            telefono = etRegistro2Telefono.text.toString()
            comprobarCampos(nombre, email, contra, dni, telefono)
            //registro()

        }

        //Si pulsamos el boton cancelar, vamos al layout anterior
        btnRegistro2Cancelar.setOnClickListener{
            entrarLogin()
        }

        /*
        Si pulsamos en la imagén para introducir una
         */
        imaRegistro2Foto.setOnClickListener{ view ->
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

    /*
    Método que comprueba que ninguno de los campos del registro quede vacío
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
            tiRegistro2Nombre.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }
            registro=false
            etRegistro2Nombre.requestFocus()
            tiRegistro2Nombre.setError("El nombre no puede estar vacío")
        }else{
            tiRegistro2Nombre.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etRegistro2Nombre.requestFocus()
            tiRegistro2Nombre.setError(null)
        }
        if(!mather.find()){
            tiRegistro2Email.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }

            registro=false
            etRegistro2Email.requestFocus()
            tiRegistro2Email.setError("Email: debe tener un formato adecuado")
        }else{
            tiRegistro2Email.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etRegistro2Email.requestFocus()
            tiRegistro2Email.setError(null)
        }
        if(contra.length < 5){
            tiRegistro2Contra.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }

            registro=false
            etRegistro2Contra.requestFocus()
            tiRegistro2Contra.setError("La contraseña debe tener más de 6 carácteres")
        }else{
            tiRegistro2Contra.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etRegistro2Contra.requestFocus()
            tiRegistro2Contra.setError(null)
        }
        if(dni.isEmpty()){
            tiRegistro2Dni.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }

            registro=false
            etRegistro2Dni.requestFocus()
            tiRegistro2Dni.setError("El DNI no puede estar vacío")
        }else{
            tiRegistro2Dni.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etRegistro2Dni.requestFocus()
            tiRegistro2Dni.setError(null)
        }
        if(telefono.isEmpty()){
            tiRegistro2Telefono.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }

            registro=false
            etRegistro2Telefono.requestFocus()
            tiRegistro2Telefono.setError("El telefono no puede estar vacío")
        }else{
            tiRegistro2Telefono.apply {
                boxStrokeColor = Color.parseColor("#000000")
                hintTextColor = myListBlack
            }

            etRegistro2Telefono.requestFocus()
            tiRegistro2Telefono.setError(null)
        }

        if(registro){
            registro()
        }
    }

    /*
    Rescatamos la foto del almacenamiento el telefono
     */
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

    /*
    Abirmos la cámara del dispositivo para tomar una fotografia y poder guardarla
     */
    private fun fotoCamara() {
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "Imagen")
        fotoUri = this.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)!!
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
                    Picasso.get().load(fotoUri).transform(CirculoTransformacion()).into(imaRegistro2Foto)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (requestCode == CAMARA) {

            Picasso.get().load(fotoUri).transform(CirculoTransformacion()).into(imaRegistro2Foto)

        }
    }

    /*
    Método que nos va a registrar y a meter nuestros datos en la bbdd
     */
    private fun registro() {

        nombre = etRegistro2Nombre.text.toString()
        email = etRegistro2Email.text.toString()
        contra = etRegistro2Contra.text.toString()
        dni = etRegistro2Dni.text.toString()
        telefono = etRegistro2Telefono.text.toString()
        diaFecha = etRegistro2Fecha.dayOfMonth.toString().toInt()
        mesFecha = etRegistro2Fecha.month.toString().toInt()
        anoFecha = etRegistro2Fecha.year.toString().toInt()

        //Si el email y la contraseña no está vacía
        if(email.isNotEmpty() && contra.isNotEmpty()){

            //Accedemos a la autenticacion de Firebase y creamos un nuevo usuario con dicho email y contraseña
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, contra).addOnCompleteListener {
                if (it.isSuccessful){
                    //Rescatamos el idUsuario y lo guardamos en SharedPreferences
                    idUsuario = Auth.currentUser.uid

                    val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                    prefs.putString("idUsuario", idUsuario)
                    prefs.apply()

                    Log.w("IDUSUARIO", idUsuario)
                    //Creamos un nombre aleatorio para el nombre de la fotografia y le indicamos la ruta donde va a guardarse
                    val filename = UUID.randomUUID().toString()
                    val ref = FirebaseStorage.getInstance().getReference("/fotosUsuarios/$filename")

                    //Si no hemos seleccionado ninguna foto
                    if(fotoUri == null){
                        //Cogemos una foto por defecto
                        var fotoVieja = Uri.parse("android.resource://com.sheilalberto.alquilsaguarro/" + R.drawable.user)
                        ref.putFile(fotoVieja!!).addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener {

                                foto = it.toString()
                                //Creamos un nuevo usuario con los datos recogidos en el Registro y lo metemos en la base de datos con nuestro id correspondiente
                                val u = Usuario(idUsuario, nombre, email, contra, foto, dni, diaFecha, mesFecha, anoFecha, telefono, false)
                                databaseReference.child(idUsuario).setValue(u)


                            }
                        }

                    } else {
                        //Rescatamos la foto que hayamos seleccionado y añadimos un nuevo usuario con dicha foto y los
                        //datos introducidos en los campos
                        ref.putFile(fotoUri!!).addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener {

                                foto = it.toString()

                                val u = Usuario(idUsuario, nombre, email, contra, foto, dni, diaFecha, mesFecha, anoFecha, telefono, false)
                                databaseReference.child(idUsuario).setValue(u)

                            }
                        }
                    }

                    entrarMain()

                } else  {
                    //alertaErrorRegistro()
                    Log.w(":::TAG", it.exception)
                }
            }
        }


    }

    //Metodo que nos entra en la aplicación
    private fun entrarMain(){
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }

    //Metodo que nos vuelve a llevar al login
    private fun entrarLogin(){
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }


}