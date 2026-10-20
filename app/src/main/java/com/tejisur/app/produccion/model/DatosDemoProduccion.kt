package com.tejisur.app.produccion.model

import java.time.LocalDateTime

/**
 * Registros de ejemplo para la demo del Sprint 1, de modo que la consulta
 * (HU03) no aparezca vacía al abrir la app. Usan los mismos códigos de OT y
 * lote que el catálogo del [com.tejisur.app.produccion.ui.ProduccionViewModel].
 *
 * Casos cubiertos:
 * - L-0101 y L-0201: merma dentro del umbral.
 * - L-0102: dos mermas que suman 7.0 % (supera el umbral).
 * - L-0301: consumo en metros, 6.0 % (supera el umbral).
 * - L-0302: consumo sin merma (no aparece en la consulta).
 */
internal object DatosDemoProduccion {

    private const val OPERARIO_DEMO = "OP-001"

    fun consumos(ahora: LocalDateTime): List<ConsumoMateriaPrima> = listOf(
        ConsumoMateriaPrima(1, "OT-2026-001", "L-0101", 120.0, UnidadMedida.KG, TipoFibra.PIMA, ahora.minusDays(3), OPERARIO_DEMO),
        ConsumoMateriaPrima(2, "OT-2026-001", "L-0102", 80.0, UnidadMedida.KG, TipoFibra.PIMA, ahora.minusDays(2), OPERARIO_DEMO),
        ConsumoMateriaPrima(3, "OT-2026-002", "L-0201", 60.0, UnidadMedida.KG, TipoFibra.ALPACA, ahora.minusDays(1), OPERARIO_DEMO),
        ConsumoMateriaPrima(4, "OT-2026-003", "L-0301", 50.0, UnidadMedida.METROS, TipoFibra.ALPACA, ahora.minusHours(5), OPERARIO_DEMO),
        ConsumoMateriaPrima(5, "OT-2026-003", "L-0302", 40.0, UnidadMedida.KG, TipoFibra.PIMA, ahora.minusHours(3), OPERARIO_DEMO)
    )

    fun mermas(ahora: LocalDateTime): List<MermaProduccion> = listOf(
        MermaProduccion(6, "OT-2026-001", "L-0101", EtapaProductiva.CORTE, 3.84, UnidadMedida.KG, ahora.minusDays(3).plusHours(2), OPERARIO_DEMO),
        MermaProduccion(7, "OT-2026-001", "L-0102", EtapaProductiva.CORTE, 4.0, UnidadMedida.KG, ahora.minusDays(2).plusHours(1), OPERARIO_DEMO),
        MermaProduccion(8, "OT-2026-001", "L-0102", EtapaProductiva.CONFECCION, 1.6, UnidadMedida.KG, ahora.minusDays(2).plusHours(4), OPERARIO_DEMO),
        MermaProduccion(9, "OT-2026-002", "L-0201", EtapaProductiva.CONFECCION, 2.1, UnidadMedida.KG, ahora.minusDays(1).plusHours(2), OPERARIO_DEMO),
        MermaProduccion(10, "OT-2026-003", "L-0301", EtapaProductiva.CORTE, 3.0, UnidadMedida.METROS, ahora.minusHours(4), OPERARIO_DEMO)
    )

    /** Primer id libre para los registros que se creen desde los formularios. */
    const val SIGUIENTE_ID = 11L
}
