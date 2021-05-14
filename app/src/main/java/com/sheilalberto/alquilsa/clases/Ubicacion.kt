package com.sheilalberto.alquilsa.clases

import java.io.Serializable

class Ubicacion : Serializable {

    var idUbicacion: String
    var idUsuario: String
    var nombre: String
    var latitud: String
    var longitud: String


    constructor(idUbicacion: String, idUsuario: String, nombre:String, latitud: String, longitud: String){
        this.idUbicacion = idUbicacion
        this.idUsuario = idUsuario
        this.nombre = nombre
        this.latitud = latitud
        this.longitud = longitud

    }




}