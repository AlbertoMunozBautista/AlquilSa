package com.sheilalberto.alquilsa

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Para que desaparezcan las partes de arriba de la pantalla
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        this.supportActionBar!!.hide()

        setContentView(R.layout.activity_splash)
        //Cargamos una animacion que va a hacer que el logo aparezca hacia arriba
        val animation1 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_arriba)
        imaSplashLogo.setAnimation(animation1)


        //Lanzamos el splash y ponemos una duración de 3 segundos
        Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)

    }
}