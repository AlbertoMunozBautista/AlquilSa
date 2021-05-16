package com.sheilalberto.alquilsa.clases

import java.io.Serializable

class Alquiler   : Serializable {

    var idReserva: String
    var fechaIni: String
    var fechaFin: String
    var idUsuario: String
    var idCoche: String
    var precioTotal : String


    constructor(idReserva: String, fechaIni: String, fechaFin:String, idUsuario: String, idCoche: String, precioTotal: String){

        this.idReserva = idReserva
        this.fechaIni = fechaIni
        this.fechaFin = fechaFin
        this.idUsuario = idUsuario
        this.idCoche = idCoche
        this.precioTotal = precioTotal

    }

}