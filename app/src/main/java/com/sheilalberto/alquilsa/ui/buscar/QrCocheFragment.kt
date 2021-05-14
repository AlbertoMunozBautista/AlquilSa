package com.sheilalberto.alquilsa.ui.buscar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Alquiler
import com.sheilalberto.alquilsa.clases.Coche
import java.util.*

class QrCocheFragment(private val c : Coche, private val total: Float, private val fInicio:String, private val fFin:String) : Fragment() {

    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseCochesReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val root =  inflater.inflate(R.layout.fragment_qr, container, false)

        //Recogemos los componentes del layout en las variables
        val imgQRQR : ImageView = root.findViewById(R.id.imgQRQR)
        val btnQRVolver : Button = root.findViewById(R.id.btnQRVolver)

        //Instanciamos las bases de datos
        db = FirebaseDatabase.getInstance("https://alquilsaguarro-default-rtdb.europe-west1.firebasedatabase.app/")
        databaseReference = db.reference.child("alquileres")//Tabla alquileres
        databaseCochesReference = db.reference.child("coches")//Tabla coches


        val prefs = activity?.getSharedPreferences(
            getString(R.string.prefs_file),
            Context.MODE_PRIVATE
        )

        //Rescatamos el idUsuario de las SharedPreferences
        val idUsuario = prefs?.getString("idUsuario", "null").toString()
        //Generamos aleatoriamente un idReeserva
        val idReserva = UUID.randomUUID().toString()

        //Creamos un nuevo alquiler
        val a = Alquiler(idReserva, fInicio, fFin, idUsuario , c.idCoche, total.toString())
        //y lo añadimos dentro de nuestro idReserva en la bbdd
        databaseReference.child(idReserva).setValue(a)
        //Ponemos a false la disponibilidad de dicho coche
        databaseCochesReference.child(c.idCoche).child("disponible").setValue("false")

        //Generamos un string con toda la informacion de la reserva
        var text = "IdReserva: " + idReserva + "\nFecha Inicio: " + fInicio +
                "\nFecha Fin: " + fFin + "\nIdUsuario: " + idUsuario + "\nIdCoche: " +
                c.idCoche + "\nTotal a pagar: " + total.toString()

        //Generamos un codigoQR con el string que hemos creado con la informacion de dicha reserva
        val bitmap = generateQRCode(text)
        imgQRQR.setImageBitmap(bitmap)



        /*
        Si pulsamos el boton volver, nos lleva al fragment anterior
         */
        btnQRVolver.setOnClickListener{
            val lugarDetalle = BuscarFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.add(R.id.QRLayout, lugarDetalle)
            transaction.addToBackStack(null)
            transaction.commit()

        }

        return root
    }


    private fun generateQRCode(text: String): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLUE else Color.WHITE)
                }
            }
        } catch (e: WriterException) {
            Log.d("QR", "generateQRCode: ${e.message}")
        }
        return bitmap
    }

}