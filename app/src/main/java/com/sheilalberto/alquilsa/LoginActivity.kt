package com.sheilalberto.alquilsa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sheilalberto.alquilsa.clases.Usuario
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var Auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var db: FirebaseDatabase
    private var idUsuario = ""

    private var contra = ""
    private var correo = ""

    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        Auth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://alquilsa-default-rtdb.europe-west1.firebasedatabase.app/")

        initPermisos()

        tvLoginRegistro.setOnClickListener{

            val mainIntent = Intent(this, RegistroActivity2::class.java)
            startActivity(mainIntent)
        }

        btnLoginInicio.setOnClickListener{
            login()
        }

        btnLoginGoogle.setOnClickListener{
            loginGoogle()
        }

    }


    private fun loginGoogle() {

        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        val googleClient = GoogleSignIn.getClient(this, googleConf)
        googleClient.signOut()

        startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account = task.getResult(ApiException::class.java)

                if (account != null){

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                        if (it.isSuccessful){


                            databaseReference = db.reference.child("usuarios")

                            idUsuario = Auth.currentUser.uid
                            //Log.e("IDUSUARIO", idUsuario)

                            val prefs = getSharedPreferences(
                                    getString(R.string.prefs_file),
                                    Context.MODE_PRIVATE
                            ).edit()
                            prefs.putString("idUsuario", idUsuario)
                            prefs.apply()

                            var getData = object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    if (!snapshot.hasChild(idUsuario)) {

                                        val u = Usuario(
                                                idUsuario,
                                                account.displayName.toString(),
                                                account.email.toString(),
                                                "******",
                                                account.photoUrl.toString(),
                                                "",
                                                1,
                                                1,
                                                2000,
                                                "",
                                                true
                                        )
                                        databaseReference.child(idUsuario).setValue(u)

                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("NOMBRE", error.message.toString())
                                }
                            }

                            databaseReference.addValueEventListener(getData)
                            databaseReference.addListenerForSingleValueEvent(getData)

                            entrarMain()

                            //guardarDatos(account.email ?: "")

                        } else  {
                            Log.w(":::TAG", it.exception)
                            //alertaErrorRegistro()
                        }

                    }

                }

            } catch (e: ApiException) {
                //alertaErrorRegistro()
                Log.w(":::TAG", e.message.toString())
            }


        }
    }

    private fun login() {

        etLoginEmail.setError(null)
        etLoginPass.setError(null)

        correo = etLoginEmail.text.toString()
        contra = etLoginPass.text.toString()

        if(comprobar(correo, contra)){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(correo, contra).addOnCompleteListener {
                if (it.isSuccessful){

                    idUsuario = Auth.currentUser.uid
                    //Log.e("IDUSUARIO", idUsuario)

                    val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE
                    ).edit()
                    prefs.putString("idUsuario", idUsuario)
                    prefs.apply()

                    entrarMain()
                } else  {
                    Log.w(":::TAG", it.exception)
                    alertaErrorRegistro()
                }
            }
        } else {
            //alertaErrorRegistro()
        }

    }

    @SuppressLint("ResourceType")
    private fun comprobar(correo: String, contra: String): Boolean {

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

        var devolver = true

        val pattern: Pattern = Pattern
                .compile(
                        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
                )

        val mather: Matcher = pattern.matcher(correo)

        if (correo.isEmpty()){
            devolver = false
            etLoginEmail.requestFocus()
            etLoginEmail.setError("El correo no puede estar vacío")
            tiLoginEmail.apply {
                boxStrokeColor = Color.parseColor("#C81B1B")
                hintTextColor = myList
            }


        } else if (contra.isEmpty()){
            devolver = false
            etLoginPass.requestFocus()
            etLoginPass.setError("La contraseña no puede estar vacía")
        } else if (contra.length < 6){
            devolver = false
            etLoginPass.requestFocus()
            etLoginPass.setError("La contraseña tiene que tener mínimo 6 caracteres")
        } else if (!mather.find()){
            devolver = false
            etLoginEmail.requestFocus()
            etLoginEmail.setError("Introduzca un correo válido")
        }

        return devolver
    }

    private fun entrarMain(){

        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }


    private fun alertaErrorRegistro(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("El correo y la contraseña no coindicen")
        builder.setPositiveButton("Aceptar", null)
        val dialog : AlertDialog = builder.create()
        dialog.show()

    }



    /**
     * Función con la que vamos a comprobar los permisos de la aplicación
     * lo vamos a hacer con la librería dexter
     */
    private fun initPermisos() {

        Dexter.withContext(this)
                //Permisos que queremos comprobar
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET
                )

                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        //si le damos todos los permisos nos saltará un toast diciendolo
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(
                                    applicationContext,
                                    "Permisos concedidos",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }

                        //si no tenemos todos los permisos nos lo recordará
                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(applicationContext, "Faltan permisos!", Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permissions: List<PermissionRequest?>?,
                            token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { Toast.makeText(
                        applicationContext,
                        "Existe errores! ",
                        Toast.LENGTH_SHORT
                ).show() }
                .onSameThread()
                .check()
    }



}