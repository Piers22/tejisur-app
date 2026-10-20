package com.tejisur.app.produccion

import com.tejisur.app.produccion.model.AnalisisMerma
import com.tejisur.app.produccion.model.ConsumoMateriaPrima
import com.tejisur.app.produccion.model.EtapaProductiva
import com.tejisur.app.produccion.model.FiltrosConsultaMerma
import com.tejisur.app.produccion.model.MermaProduccion
import com.tejisur.app.produccion.model.TipoFibra
import com.tejisur.app.produccion.model.UnidadMedida
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/** Pruebas de los criterios de aceptación de HU03 y HU16. */
class AnalisisMermaTest {

    private val dia1: LocalDateTime = LocalDateTime.of(2026, 10, 14, 8, 0)
    private val dia2: LocalDateTime = dia1.plusDays(1)
    private val dia3: LocalDateTime = dia1.plusDays(2)
    private val umbral = 5.0

    private fun consumo(
        ot: String, lote: String, cantidad: Double, fibra: TipoFibra,
        fecha: LocalDateTime = dia1, unidad: UnidadMedida = UnidadMedida.KG
    ) = ConsumoMateriaPrima(0, ot, lote, cantidad, unidad, fibra, fecha, "OP-TEST")

    private fun merma(
        ot: String, lote: String, cantidad: Double,
        fecha: LocalDateTime = dia1, unidad: UnidadMedida = UnidadMedida.KG
    ) = MermaProduccion(0, ot, lote, EtapaProductiva.CORTE, cantidad, unidad, fecha, "OP-TEST")

    // ------------------------------------------------------------------ HU03

    @Test
    fun porcentaje_esMermaEntreConsumoPor100_sumandoRegistrosDelLote() {
        val resumenes = AnalisisMerma.resumirPorLote(
            consumos = listOf(
                consumo("OT-1", "L-1", 50.0, TipoFibra.PIMA),
                consumo("OT-1", "L-1", 30.0, TipoFibra.PIMA)
            ),
            mermas = listOf(merma("OT-1", "L-1", 4.0), merma("OT-1", "L-1", 1.6))
        )

        val lote = resumenes.single()
        assertEquals(80.0, lote.consumoTotal, 1e-9)
        assertEquals(5.6, lote.mermaTotal, 1e-9)
        assertEquals(7.0, lote.porcentajeMerma, 1e-9)
    }

    @Test
    fun excluyeLotesSinMerma_yMermasEnOtraUnidad() {
        val resumenes = AnalisisMerma.resumirPorLote(
            consumos = listOf(
                consumo("OT-1", "L-1", 100.0, TipoFibra.PIMA),
                consumo("OT-1", "L-2", 100.0, TipoFibra.PIMA)
            ),
            mermas = listOf(merma("OT-1", "L-2", 3.0, unidad = UnidadMedida.METROS))
        )

        assertTrue(resumenes.isEmpty())
    }

    @Test
    fun filtraPorLoteFibraYRangoDeFechasInclusive() {
        val resumenes = AnalisisMerma.resumirPorLote(
            consumos = listOf(
                consumo("OT-1", "L-1", 100.0, TipoFibra.PIMA),
                consumo("OT-1", "L-2", 100.0, TipoFibra.ALPACA),
                consumo("OT-2", "L-3", 100.0, TipoFibra.PIMA)
            ),
            mermas = listOf(
                merma("OT-1", "L-1", 2.0, fecha = dia1),
                merma("OT-1", "L-2", 3.0, fecha = dia2),
                merma("OT-2", "L-3", 4.0, fecha = dia3)
            )
        )

        val porFibra = AnalisisMerma.filtrarYOrdenar(resumenes, FiltrosConsultaMerma(tipoFibra = TipoFibra.PIMA))
        assertEquals(setOf("L-1", "L-3"), porFibra.map { it.lote }.toSet())

        val porFecha = AnalisisMerma.filtrarYOrdenar(
            resumenes,
            FiltrosConsultaMerma(fechaDesde = dia2.toLocalDate(), fechaHasta = dia3.toLocalDate())
        )
        assertEquals(setOf("L-2", "L-3"), porFecha.map { it.lote }.toSet())

        val porLote = AnalisisMerma.filtrarYOrdenar(resumenes, FiltrosConsultaMerma(lote = "L-2"))
        assertEquals(listOf("L-2"), porLote.map { it.lote })

        val combinado = AnalisisMerma.filtrarYOrdenar(
            resumenes,
            FiltrosConsultaMerma(tipoFibra = TipoFibra.PIMA, fechaHasta = LocalDate.of(2026, 10, 14))
        )
        assertEquals(listOf("L-1"), combinado.map { it.lote })
    }

    @Test
    fun ordenaPorPorcentajeDeMayorAMenor() {
        val resumenes = AnalisisMerma.resumirPorLote(
            consumos = listOf(
                consumo("OT-1", "L-1", 100.0, TipoFibra.PIMA),
                consumo("OT-1", "L-2", 50.0, TipoFibra.PIMA),
                consumo("OT-1", "L-3", 200.0, TipoFibra.PIMA)
            ),
            mermas = listOf(
                merma("OT-1", "L-1", 3.0), // 3 %
                merma("OT-1", "L-2", 4.0), // 8 %
                merma("OT-1", "L-3", 2.0)  // 1 %
            )
        )

        val ordenados = AnalisisMerma.filtrarYOrdenar(resumenes, FiltrosConsultaMerma(ordenarPorPorcentaje = true))

        assertEquals(listOf("L-2", "L-1", "L-3"), ordenados.map { it.lote })
    }

    // ------------------------------------------------------------------ HU16

    @Test
    fun loteSobreElUmbral_generaAlertaConLoteOtPorcentajeYUmbral() {
        val resumen = AnalisisMerma.resumirPorLote(
            listOf(consumo("OT-9", "L-9", 80.0, TipoFibra.ALPACA)),
            listOf(merma("OT-9", "L-9", 5.6))
        ).single()

        val alerta = AnalisisMerma.evaluarAlerta(resumen, umbral, dia1)

        assertNotNull(alerta)
        assertEquals("L-9", alerta!!.lote)
        assertEquals("OT-9", alerta.ordenTrabajo)
        assertEquals(7.0, alerta.porcentajeMerma, 1e-9)
        assertEquals(umbral, alerta.umbral, 1e-9)
    }

    @Test
    fun loteDentroDelUmbral_oExactamenteEnElUmbral_noGeneraAlerta() {
        val dentro = AnalisisMerma.resumirPorLote(
            listOf(consumo("OT-1", "L-1", 100.0, TipoFibra.PIMA)),
            listOf(merma("OT-1", "L-1", 3.0))
        ).single()
        val enElLimite = AnalisisMerma.resumirPorLote(
            listOf(consumo("OT-1", "L-2", 100.0, TipoFibra.PIMA)),
            listOf(merma("OT-1", "L-2", 5.0))
        ).single()

        assertNull(AnalisisMerma.evaluarAlerta(dentro, umbral))
        assertNull(AnalisisMerma.evaluarAlerta(enElLimite, umbral))
    }

    @Test
    fun loteSinConsumo_noGeneraAlerta() {
        assertNull(AnalisisMerma.evaluarAlerta(null, umbral))
    }
}
