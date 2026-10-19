package com.tejisur.app.produccion.ui

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.StringRes

/** Permite usar una lambda en lugar del listener de dos métodos del Spinner. */
internal inline fun Spinner.alSeleccionar(crossinline accion: (posicion: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
            accion(position)

        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }
}

/** Crea un adapter cuya posición 0 es el texto guía ("Seleccione..."). */
internal fun Context.adaptadorConHint(hint: String, items: List<String>): ArrayAdapter<String> =
    ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(hint) + items).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

/** Muestra u oculta un TextView de error a partir de un recurso de strings.xml. */
internal fun TextView.mostrarError(@StringRes mensaje: Int?) {
    if (mensaje == null) {
        text = null
        visibility = View.GONE
    } else {
        setText(mensaje)
        visibility = View.VISIBLE
    }
}
