package com.tejisur.app.produccion.ui

/**
 * Envoltorio para eventos de una sola vez expuestos por LiveData (por ejemplo,
 * la alerta de HU16). Evita que el diálogo vuelva a mostrarse al rotar la
 * pantalla o al volver a observar el LiveData.
 */
class Evento<out T>(private val contenido: T) {

    var fueManejado: Boolean = false
        private set

    /** Devuelve el contenido solo la primera vez; luego devuelve null. */
    fun obtenerSiNoManejado(): T? =
        if (fueManejado) {
            null
        } else {
            fueManejado = true
            contenido
        }
}
