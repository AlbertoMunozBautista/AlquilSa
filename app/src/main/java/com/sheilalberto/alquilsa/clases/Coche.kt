package com.sheilalberto.alquilsa.clases

import java.io.Serializable

class Coche : Serializable {
    var idCoche: String
    var matricula: String
    var idUsuario: String
    var foto: String
    var nombre: String
    var likes: Int
    var idUbicacion: String
    var autonomia: String
    var combustible: String
    var transmision: String
    var asientos: String
    var tipo : String
    var precio : Float
    var disponible: Boolean

    constructor(idCoche: String, matricula: String, idUsuario: String, foto: String, nombre:String, likes: Int, idUbicacion: String,
                autonomia: String, combustible: String, transmision: String, asientos: String, tipo: String,
                precio: Float, disponible: Boolean){

        this.idCoche = idCoche
        this.matricula = matricula
        this.idUsuario = idUsuario
        this.foto = foto
        this.nombre = nombre
        this.likes = likes
        this.idUbicacion = idUbicacion
        this.autonomia = autonomia
        this.combustible = combustible
        this.transmision = transmision
        this.asientos = asientos
        this.tipo = tipo
        this.precio = precio
        this.disponible = disponible

    }




}