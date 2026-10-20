package com.tejisur.app.produccion.model

import androidx.annotation.StringRes
import com.tejisur.app.R
import java.time.LocalDateTime

/** Etapas productivas donde se genera merma (HU02). */
enum class EtapaProductiva(@StringRes val etiquetaRes: Int) {
    CORTE(R.string.etapa_corte),
    CONFECCION(R.string.etapa_confeccion)
}

/**
 * HU02 - Registro de merma de producción.
 * Entidad inmutable que se genera solo cuando el formulario es válido.
 */
data class MermaProduccion(
    val id: Long,
    val ordenTrabajo: String,
    val lote: String,
    val etapa: EtapaProductiva,
    val cantidad: Double,
    val unidad: UnidadMedida,
    val fechaHora: LocalDateTime,
    val operario: String
) {
    init {
        require(cantidad > 0) { "La cantidad de merma debe ser mayor a 0" }
    }
}
