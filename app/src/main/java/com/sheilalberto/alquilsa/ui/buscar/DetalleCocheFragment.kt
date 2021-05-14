package com.sheilalberto.alquilsa.ui.buscar

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Coche
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DetalleCocheFragment(
    private val c: Coche,
    var listaIdsCoches: MutableList<String> = mutableListOf<String>(),
    private val fabBuscarFiltro: FloatingActionButton,
    private val idUbicacion : String,) : Fragment() {

    private lateinit var fechaInicio : Date

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root =  inflater.inflate(R.layout.fragment_detalle_coche, container, false)

        //Recogemos los componentes del layout en las variables
        val tvDetalleNombreCoche: TextView = root.findViewById(R.id.tvDetalleNombreCoche)
        val imgDetalleFotoCoche : ImageView = root.findViewById(R.id.imageView10)
        val tvDetalleGasolina: TextView = root.findViewById(R.id.tvDetalleGasolina)
        val tvDetalleMarchas: TextView = root.findViewById(R.id.tvDetalleMarchas)
        val tvDetalleKm: TextView = root.findViewById(R.id.tvDetalleKm)
        val tvDetalleNumPersonas: TextView = root.findViewById(R.id.tvDetalleNumPersonas)
        val tvDetalleMatricula: TextView = root.findViewById(R.id.tvDetalleMatricula)
        val tvDetallePrecio: TextView = root.findViewById(R.id.tvDetallePrecio)
        val tvDetalleFechaR : TextView = root.findViewById(R.id.tvDetalleFechaR)
        val tvDetalleFechaD : TextView = root.findViewById(R.id.tvDetalleFechaD)
        val btnDetalleCancelar : Button = root.findViewById(R.id.btnDetalleCancelar)
        val btnDetalleContinuar : Button = root.findViewById(R.id.btnDetalleContinuar)

        var listaCochesFiltro = listaIdsCoches
        var fabBoton = fabBuscarFiltro
        var idUbica = idUbicacion



        //Ponemos en los campos del layout la información que tenga el coche recibido
        tvDetalleNombreCoche.text = c.nombre
        Picasso.get().load(Uri.parse(c.foto)).into(imgDetalleFotoCoche)
        tvDetalleGasolina.text = c.combustible
        tvDetalleMarchas.text = c.transmision
        tvDetalleKm.text = c.autonomia + " Km"
        tvDetalleNumPersonas.text = c.asientos
        tvDetalleMatricula.text = c.matricula
        tvDetallePrecio.text = c.precio.toString() + " €"

        //Le indicamos que formato queremos que tenga la fecha
        val date = LocalDateTime.now()
        tvDetalleFechaR.text = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date)

        //Hacemos que se abra un datePickerPicker dialog para seleccionar la fecha de reserva
        tvDetalleFechaR.setOnClickListener(){
            val date = LocalDateTime.now()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, mYear, mMonth, mDay ->
                    tvDetallePrecio.text = (mDay.toString() + "/" + (mMonth + 1) + "/" + mYear)
                }, date.year, date.monthValue - 1, date.dayOfMonth
            )
            datePickerDialog.show()
        }


        val aa= SimpleDateFormat("dd/MM/yyyy")
        var dateaa  = Date(aa.parse(tvDetalleFechaR.text.toString()).time)

        //Hacemos que se abra un datePickerPicker dialog para seleccionar la fecha de devolucion
        tvDetalleFechaD.text = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date)
        tvDetalleFechaD.setOnClickListener(){
            val date = LocalDateTime.now()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, mYear, mMonth, mDay ->
                    tvDetalleFechaD.text = (mDay.toString() + "/" + (mMonth + 1) + "/" + mYear)
                }, date.year, date.monthValue - 1, date.dayOfMonth
            )
            datePickerDialog.show()
            datePickerDialog.getDatePicker().setMinDate(dateaa.time);
        }



        /*
        Si pulsamos el boton cancelar, nos muestra el fragment de antes
         */
        btnDetalleCancelar.setOnClickListener{
            val lugarDetalle = ListaCochesFragment(fabBoton, idUbica, listaCochesFiltro)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.add(R.id.detalle_coche_layout, lugarDetalle)
            transaction.addToBackStack(null)
            transaction.commit()

        }

        /*
        Si pulsamos el boton continuar
         */
        btnDetalleContinuar.setOnClickListener(){
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val dateInicio = Date(sdf.parse(tvDetalleFechaR.text.toString()).getTime())
            val dateFin = Date(sdf.parse(tvDetalleFechaD.text.toString()).getTime())

            //Comprobamos que la fecha de devolución sea posterior a la de reserva
            if(dateFin.getTime() <= dateInicio.getTime()){
                Toast.makeText(requireActivity(), "¡Fecha mal introducida!", Toast.LENGTH_SHORT).show()
                //Abrimos el fragment de pago coche
            }else{
                val editarFragment = PagoCocheFragment(tvDetalleFechaR.text.toString(), tvDetalleFechaD.text.toString(), c)
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transaction.replace(R.id.detalle_coche_layout, editarFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }

        return root
    }


}