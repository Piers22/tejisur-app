package com.tejisur.app.produccion.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * HU03 - Resultado del análisis de merma de un lote.
 *
 * Solo existe para lotes con consumo mayor que cero y merma registrada en la
 * MISMA unidad de medida; de lo contrario el porcentaje no tendría sentido.
 */
data class ResumenMermaLote(
    val ordenTrabajo: String,
    val lote: String,
    val tipoFibra: TipoFibra,
    val unidad: UnidadMedida,
    val consumoTotal: Double,
    val mermaTotal: Double,
    /** Fecha del último registro de merma del lote (se usa para filtrar). */
    val fecha: LocalDate
) {
    init {
        require(consumoTotal > 0.0) { "El consumo del lote debe ser mayor a 0" }
        require(mermaTotal >= 0.0) { "La merma del lote no puede ser negativa" }
    }

    /** Porcentaje de merma = merma / consumo × 100. */
    val porcentajeMerma: Double
        get() = mermaTotal / consumoTotal * 100.0

    /** HU16 - Estrictamente mayor: un lote exactamente en el umbral sigue siendo tolerable. */
    fun superaUmbral(umbral: Double): Boolean = porcentajeMerma > umbral
}

/** HU03 - Criterios de consulta. `null` significa "sin filtro". */
data class FiltrosConsultaMerma(
    val lote: String? = null,
    val tipoFibra: TipoFibra? = null,
    val fechaDesde: LocalDate? = null,
    val fechaHasta: LocalDate? = null,
    val ordenarPorPorcentaje: Boolean = false
) {
    val tieneFiltrosActivos: Boolean
        get() = lote != null || tipoFibra != null || fechaDesde != null ||
            fechaHasta != null || ordenarPorPorcentaje
}

/** HU16 - Datos que se muestran al jefe de planta cuando un lote supera el umbral. */
data class AlertaMerma(
    val ordenTrabajo: String,
    val lote: String,
    val porcentajeMerma: Double,
    val umbral: Double,
    val fechaHora: LocalDateTime
)
