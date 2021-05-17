package com.sheilalberto.alquilsa

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference


    private var idUsuario = ""
    private var name: String? = ""
    private var email: String? = ""
    private var photoUrl: String? = ""

    private lateinit var Auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_buscar, R.id.nav_poner, R.id.nav_chat, R.id.nav_perfil, R.id.nav_ubicacion), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        //navView.menu.findItem(R.id.nav_salir).setOnMenuItemClickListener ({ menuItem  })

        Auth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://alquilsaguarro-default-rtdb.europe-west1.firebasedatabase.app/")


        val tvMainCorreo : TextView = navView.getHeaderView(0).findViewById(R.id.tvMainCorreo)
        val tvMainNombre : TextView = navView.getHeaderView(0).findViewById(R.id.tvMainNombre)
        val imaMainFoto : ImageView = navView.getHeaderView(0).findViewById(R.id.imaMainFoto)

        val prefs = this.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        idUsuario = prefs?.getString("idUsuario", "null").toString()
        Log.e("NOMBRE", "ID" + idUsuario.toString())

        databaseReference = db.reference.child("usuarios").child(idUsuario)

        var getData = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                name = snapshot.child("nombre").getValue().toString()
                //Log.e("NOMBRE","PRIMERO" + name.toString())
                email = snapshot.child("email").getValue().toString()
                //Log.e("NOMBRE", "SEGUNDO" +email.toString())
                photoUrl = snapshot.child("foto").getValue().toString()
                //Log.e("NOMBRE", "SEGUNDO" +photoUrl.toString())

                tvMainNombre.text = name
                tvMainCorreo.text = email

                Picasso.get()
                    // .load(R.drawable.user_avatar)
                    .load(Uri.parse(photoUrl))
                    .transform(CirculoTransformacion())
                    .resize(130, 130)
                    .into(imaMainFoto)

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NOMBRE", error.message.toString())
            }
        }

        databaseReference.addValueEventListener(getData)
        databaseReference.addListenerForSingleValueEvent(getData)






        //navView2.setNavigationItemSelectedListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.menu_cerrar -> {
                salir()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun salir() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.logo_negro)
            .setTitle("Cerrar sesión actual")
            .setMessage("¿Desea salir de la sesión actual?")
            .setPositiveButton("Sí"){ dialog, which -> cerrarSesion()}
            .setNegativeButton( "No",null)
            .show()
    }

    private fun cerrarSesion(){

        //Se borran las shared preferences
        val prefs = this.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)?.edit()
        prefs?.clear()
        prefs?.apply()

        //Se cierra sesion del firebase
        FirebaseAuth.getInstance().signOut()

        //Se vuelve al login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

    }

}