package com.tejisur.app.produccion.model

import androidx.annotation.StringRes
import com.tejisur.app.R
import java.time.LocalDateTime

/** Unidades de medida admitidas para consumo y merma (HU01 / HU02). */
enum class UnidadMedida(val simbolo: String) {
    KG("kg"),
    METROS("m")
}

/** Tipos de fibra trabajados por TEJISUR (HU01). */
enum class TipoFibra(@StringRes val etiquetaRes: Int) {
    PIMA(R.string.fibra_pima),
    ALPACA(R.string.fibra_alpaca)
}

/**
 * HU01 - Registro de consumo de materia prima.
 * Entidad inmutable que se genera solo cuando el formulario es válido.
 */
data class ConsumoMateriaPrima(
    val id: Long,
    val ordenTrabajo: String,
    val lote: String,
    val cantidad: Double,
    val unidad: UnidadMedida,
    val tipoFibra: TipoFibra,
    val fechaHora: LocalDateTime,
    val operario: String
) {
    init {
        require(cantidad > 0) { "La cantidad consumida debe ser mayor a 0" }
    }
}
