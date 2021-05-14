package com.sheilalberto.alquilsa.ui.buscar

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction
import com.sheilalberto.alquilsa.R
import com.sheilalberto.alquilsa.clases.Coche
import kotlinx.android.synthetic.main.fragment_pago_coche.*
import java.text.SimpleDateFormat
import java.util.*

class PagoCocheFragment (private val fInicio:String, private val fFin:String, private val c : Coche) : Fragment() {

    private var titular = ""
    private var numero = ""
    private var csv = ""
    private var total = 0.0f
    private var pago = ""



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root =  inflater.inflate(R.layout.fragment_pago_coche, container, false)

        //Recogemos los componentes del layout en las variables
        var rbPago: RadioGroup = root.findViewById(R.id.rbPago)
        var btnPagoAlquilar : Button = root.findViewById(R.id.btnPagoAlquilar)
        var btnPagoCancelar: Button = root.findViewById(R.id.btnPagoCancelar)
        var etPagoTitular : EditText = root.findViewById(R.id.etPagoTitular)
        var etPagoNumero : EditText = root.findViewById(R.id.etPagoNumero)
        var etPagoCSV : EditText = root.findViewById(R.id.etPagoCSV)
        var etPagoTotal : TextView = root.findViewById(R.id.tvPagoTotalPagar)

        //Guardamos en total el pago que se debe efectuar por ese alquiler
        total = calcularPago()
        //Mostramos el precio que se ha calculado
        etPagoTotal.setText(total.toString()+" € ")

        //Nos permite que solo podamos seleccionar una opción del RadioGroup
        for (i in 0 until rbPago.getChildCount()) {
            (rbPago.getChildAt(i) as RadioButton).isChecked = true
        }

        /*
        Si pinchamos en el boton cancelar, nos lleva al fragment anterior
         */
        btnPagoCancelar.setOnClickListener{
            val editarFragment = BuscarFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.fragment_pago, editarFragment)
            transaction.addToBackStack(null)
            transaction.commit()

        }

        /*
        Si pulsamos el boton alquilar, recogemos la informacion que hemos introducido
        en los campos y la mandamos al metodo comprobarCampos() para asegurarnos de que
        no puedan estar vacios
         */
        btnPagoAlquilar.setOnClickListener(){

            titular = etPagoTitular.text.toString()
            numero = etPagoNumero.text.toString()
            csv = etPagoCSV.text.toString()


            comprobarCampos(titular, numero, csv)


        }


        return root
    }

    /*
    Comprueba que los campos no puedan estar vacios, mostrando un error en caso de que eso ocurra
     */
    private fun comprobarCampos(titular: String, numero: String, csv: String) {

        var alquilo = true

        if (titular.isEmpty()) {
            alquilo = false
            etPagoTitular.setError("El nombre no puede estar vacío")
        } else {
            etPagoTitular.setError(null)
        }

        if (numero.isEmpty()) {
            alquilo = false
            etPagoNumero.setError("El nombre no puede estar vacío")
        } else {
            etPagoNumero.setError(null)
        }

        if (csv.isEmpty()) {
            alquilo = false
            etPagoCSV.setError("El nombre no puede estar vacío")
        } else {
            etPagoCSV.setError(null)
        }

        if(alquilo){
            Toast.makeText(requireActivity(), "¡TODO HA SALIDO BIEN!", Toast.LENGTH_SHORT).show()

            val editarFragment = QrCocheFragment(c, total, fInicio, fFin)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.fragment_pago, editarFragment)
            transaction.addToBackStack(null)
            transaction.commit()

        }else{
            Toast.makeText(requireActivity(), "¡Revisa el pago!", Toast.LENGTH_SHORT).show()
        }

    }

    /*
    Calculamos el total que debe pagar el usuario por el coche indicado según los dias de reserva
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularPago(): Float {

        //Pasamos las fechas de reserva y de devolucion a un formato en concreto
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val dateInicio = Date(sdf.parse(fInicio).getTime())
        val dateFin = Date(sdf.parse(fFin).getTime())

        //Restamos las fechas para calcular los dias que el coche esta reservado
        val diff: Long = dateFin.getTime() - dateInicio.getTime()

        val segundos = diff / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24

        Log.e("TIEMPO", dias.toString())

        //Multiplicamos esos dias por el precio estimado para el coche seleccionado
        total = dias * c.precio
        Log.e("TIEMPO", "total= "+total.toString())

        return total
    }


}