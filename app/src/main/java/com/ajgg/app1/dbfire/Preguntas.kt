package com.ajgg.app1.dbfire

import java.util.*

/**
 * Para guardar los valores que quiero introducir/actualizar en la base de datos
 * Contiene un HashMap con los datos, ya que las funciones que utilizaré necesitan como parámetro
 * un HashMap
 */
data class Preguntas(var id: String="", var respuesta: String="")
