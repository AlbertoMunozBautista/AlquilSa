package com.sheilalberto.alquilsa.clases

import java.io.Serializable

class Usuario : Serializable {
    var idUsuario: String
    var nombre: String
    var email: String
    var contra: String
    var foto: String
    var dni: String = ""
    var diaFecha: Int = 0
    var mesFecha: Int = 0
    var anoFecha: Int = 0
    var telefono: String = ""
    var google : Boolean = false

    constructor(idUsuario: String, nombre: String, email:String, contra: String, foto: String, dni: String, diaFecha: Int, mesFecha: Int, anoFecha: Int, telefono: String, google: Boolean){
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.email = email
        this.contra = contra
        this.foto = foto
        this.dni = dni
        this.diaFecha = diaFecha
        this.mesFecha = mesFecha
        this.anoFecha = anoFecha
        this.telefono = telefono
        this.google = google
    }

    constructor(idUsuario: String, nombre: String, email: String, contra: String, foto: String) {
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.email = email
        this.contra = contra
        this.foto = foto
    }


}