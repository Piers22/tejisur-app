package com.tejisur.app.produccion.model

import java.time.LocalDateTime

/**
 * HU03 / HU16 - Reglas de negocio del análisis de merma.
 *
 * Es Kotlin puro (sin dependencias de Android) para poder probarlo con tests
 * unitarios locales y reutilizarlo desde el ViewModel.
 */
object AnalisisMerma {

    /** Un lote se analiza por OT + lote + unidad: kg y metros no se mezclan. */
    private data class ClaveLote(
        val ordenTrabajo: String,
        val lote: String,
        val unidad: UnidadMedida
    )

    /**
     * Agrupa consumos y mermas por lote y calcula su porcentaje de merma.
     *
     * Se descartan:
     * - lotes sin merma registrada (no hay nada que analizar);
     * - mermas cuya unidad no coincide con la del consumo del lote;
     * - lotes cuyo consumo total no es mayor que cero.
     *
     * La fibra se toma del consumo más reciente del lote, porque la merma
     * (HU02) no registra tipo de fibra.
     */
    fun resumirPorLote(
        consumos: List<ConsumoMateriaPrima>,
        mermas: List<MermaProduccion>
    ): List<ResumenMermaLote> {
        val mermasPorLote = mermas.groupBy { ClaveLote(it.ordenTrabajo, it.lote, it.unidad) }

        return consumos
            .groupBy { ClaveLote(it.ordenTrabajo, it.lote, it.unidad) }
            .mapNotNull { (clave, consumosLote) ->
                val mermasLote = mermasPorLote[clave].orEmpty()
                val consumoTotal = consumosLote.sumOf { it.cantidad }
                if (mermasLote.isEmpty() || consumoTotal <= 0.0) return@mapNotNull null

                ResumenMermaLote(
                    ordenTrabajo = clave.ordenTrabajo,
                    lote = clave.lote,
                    tipoFibra = consumosLote.maxBy { it.fechaHora }.tipoFibra,
                    unidad = clave.unidad,
                    consumoTotal = consumoTotal,
                    mermaTotal = mermasLote.sumOf { it.cantidad },
                    fecha = mermasLote.maxOf { it.fechaHora }.toLocalDate()
                )
            }
    }

    /**
     * Aplica los filtros de HU03 (lote, fibra y rango de fechas, ambos extremos
     * inclusive) y la ordenación solicitada.
     *
     * Sin ordenación explícita se muestran primero los lotes más recientes.
     */
    fun filtrarYOrdenar(
        resumenes: List<ResumenMermaLote>,
        filtros: FiltrosConsultaMerma
    ): List<ResumenMermaLote> {
        val filtrados = resumenes.filter { resumen ->
            (filtros.lote == null || resumen.lote == filtros.lote) &&
                (filtros.tipoFibra == null || resumen.tipoFibra == filtros.tipoFibra) &&
                (filtros.fechaDesde == null || !resumen.fecha.isBefore(filtros.fechaDesde)) &&
                (filtros.fechaHasta == null || !resumen.fecha.isAfter(filtros.fechaHasta))
        }

        return if (filtros.ordenarPorPorcentaje) {
            filtrados.sortedWith(
                compareByDescending<ResumenMermaLote> { it.porcentajeMerma }.thenBy { it.lote }
            )
        } else {
            filtrados.sortedWith(
                compareByDescending<ResumenMermaLote> { it.fecha }.thenBy { it.lote }
            )
        }
    }

    /**
     * HU16 - Devuelve una alerta si el lote supera el umbral; `null` si está
     * dentro del umbral o si el lote aún no puede analizarse (sin consumo).
     */
    fun evaluarAlerta(
        resumen: ResumenMermaLote?,
        umbral: Double,
        ahora: LocalDateTime = LocalDateTime.now()
    ): AlertaMerma? {
        if (resumen == null || !resumen.superaUmbral(umbral)) return null
        return AlertaMerma(
            ordenTrabajo = resumen.ordenTrabajo,
            lote = resumen.lote,
            porcentajeMerma = resumen.porcentajeMerma,
            umbral = umbral,
            fechaHora = ahora
        )
    }
}
