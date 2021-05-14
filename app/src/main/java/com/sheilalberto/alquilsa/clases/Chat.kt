package com.sheilalberto.alquilsa.clases

import java.io.Serializable

class Chat  : Serializable {

    var idEmisor: String
    var idReceptor: String
    var mensaje: String


    constructor(idEmisor: String, idReceptor: String, mensaje:String){
        this.idEmisor = idEmisor
        this.idReceptor = idReceptor
        this.mensaje = mensaje

    }




}