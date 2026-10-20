package com.tejisur.app.produccion.ui

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tejisur.app.R
import com.tejisur.app.produccion.model.AlertaMerma
import com.tejisur.app.produccion.model.AnalisisMerma
import com.tejisur.app.produccion.model.ConfiguracionPlanta
import com.tejisur.app.produccion.model.ConsumoMateriaPrima
import com.tejisur.app.produccion.model.DatosDemoProduccion
import com.tejisur.app.produccion.model.EtapaProductiva
import com.tejisur.app.produccion.model.FiltrosConsultaMerma
import com.tejisur.app.produccion.model.MermaProduccion
import com.tejisur.app.produccion.model.ResumenMermaLote
import com.tejisur.app.produccion.model.TipoFibra
import com.tejisur.app.produccion.model.UnidadMedida
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Controlador de HU01 (consumo), HU02 (merma), HU03 (consulta de mermas por
 * lote) y HU16 (alerta por merma excesiva).
 *
 * Mantiene catálogos (OT/Lotes), el estado temporal de los formularios y
 * filtros, y sus errores de validación, de modo que nada se pierde al rotar
 * la pantalla ni al cambiar de sección. Las reglas de cálculo de merma viven
 * en [AnalisisMerma]. Los índices de selección usan -1 como "sin seleccionar".
 */
class ProduccionViewModel : ViewModel() {

    // ---------------------------------------------------------------------
    // Estado de formularios y errores
    // ---------------------------------------------------------------------

    data class ConsumoFormState(
        val otIndex: Int = SIN_SELECCION,
        val loteIndex: Int = SIN_SELECCION,
        val cantidad: String = "",
        val unidad: UnidadMedida? = null,
        val tipoFibra: TipoFibra? = null
    )

    data class ConsumoErrores(
        @StringRes val ot: Int? = null,
        @StringRes val lote: Int? = null,
        @StringRes val cantidad: Int? = null,
        @StringRes val unidad: Int? = null,
        @StringRes val tipoFibra: Int? = null
    ) {
        val hayErrores: Boolean
            get() = listOf(ot, lote, cantidad, unidad, tipoFibra).any { it != null }
    }

    data class MermaFormState(
        val otIndex: Int = SIN_SELECCION,
        val loteIndex: Int = SIN_SELECCION,
        val etapa: EtapaProductiva? = null,
        val cantidad: String = "",
        val unidad: UnidadMedida? = null
    )

    data class MermaErrores(
        @StringRes val ot: Int? = null,
        @StringRes val lote: Int? = null,
        @StringRes val etapa: Int? = null,
        @StringRes val cantidad: Int? = null,
        @StringRes val unidad: Int? = null
    ) {
        val hayErrores: Boolean
            get() = listOf(ot, lote, etapa, cantidad, unidad).any { it != null }
    }

    // ---------------------------------------------------------------------
    // Catálogos simulados (en Sprints futuros vendrán de un repositorio)
    // ---------------------------------------------------------------------

    private val lotesPorOt: Map<String, List<String>> = linkedMapOf(
        "OT-2026-001" to listOf("L-0101", "L-0102", "L-0103"),
        "OT-2026-002" to listOf("L-0201", "L-0202"),
        "OT-2026-003" to listOf("L-0301", "L-0302", "L-0303", "L-0304")
    )

    val ordenesTrabajo: List<String> = lotesPorOt.keys.toList()

    val etapasProductivas: List<EtapaProductiva> = EtapaProductiva.entries

    fun lotesDe(otIndex: Int): List<String> =
        ordenesTrabajo.getOrNull(otIndex)?.let { lotesPorOt[it] }.orEmpty()

    // ---------------------------------------------------------------------
    // Registros simulados (fuente para HU03 / HU16)
    // ---------------------------------------------------------------------

    private val inicioSesion = LocalDateTime.now()

    private val _consumos = MutableLiveData<List<ConsumoMateriaPrima>>(
        if (CARGAR_DATOS_DEMO) DatosDemoProduccion.consumos(inicioSesion) else emptyList()
    )
    val consumos: LiveData<List<ConsumoMateriaPrima>> = _consumos

    private val _mermas = MutableLiveData<List<MermaProduccion>>(
        if (CARGAR_DATOS_DEMO) DatosDemoProduccion.mermas(inicioSesion) else emptyList()
    )
    val mermas: LiveData<List<MermaProduccion>> = _mermas

    private var siguienteId = if (CARGAR_DATOS_DEMO) DatosDemoProduccion.SIGUIENTE_ID else 1L

    // =====================================================================
    // HU01 - Consumo de materia prima
    // =====================================================================

    var consumoForm = ConsumoFormState()
        private set

    private val _erroresConsumo = MutableLiveData(ConsumoErrores())
    val erroresConsumo: LiveData<ConsumoErrores> = _erroresConsumo

    fun onConsumoOtSeleccionada(index: Int) {
        if (index == consumoForm.otIndex) return
        consumoForm = consumoForm.copy(otIndex = index, loteIndex = SIN_SELECCION)
        _erroresConsumo.value = erroresConsumoActuales().copy(ot = null)
    }

    fun onConsumoLoteSeleccionado(index: Int) {
        if (index == consumoForm.loteIndex) return
        consumoForm = consumoForm.copy(loteIndex = index)
        _erroresConsumo.value = erroresConsumoActuales().copy(lote = null)
    }

    fun onConsumoCantidadCambiada(texto: String) {
        if (texto == consumoForm.cantidad) return
        consumoForm = consumoForm.copy(cantidad = texto)
        _erroresConsumo.value = erroresConsumoActuales().copy(cantidad = null)
    }

    fun onConsumoUnidadSeleccionada(unidad: UnidadMedida?) {
        if (unidad == consumoForm.unidad) return
        consumoForm = consumoForm.copy(unidad = unidad)
        _erroresConsumo.value = erroresConsumoActuales().copy(unidad = null)
    }

    fun onConsumoFibraSeleccionada(fibra: TipoFibra?) {
        if (fibra == consumoForm.tipoFibra) return
        consumoForm = consumoForm.copy(tipoFibra = fibra)
        _erroresConsumo.value = erroresConsumoActuales().copy(tipoFibra = null)
    }

    /**
     * Valida el formulario de HU01. Si es válido, simula el guardado,
     * reinicia el formulario y devuelve el registro creado; si no, devuelve null.
     */
    fun registrarConsumo(): ConsumoMateriaPrima? {
        val form = consumoForm
        val errores = ConsumoErrores(
            ot = R.string.error_ot_requerida.takeIf { form.otIndex == SIN_SELECCION },
            lote = R.string.error_lote_requerido.takeIf { form.loteIndex == SIN_SELECCION },
            cantidad = validarCantidad(form.cantidad),
            unidad = R.string.error_unidad_requerida.takeIf { form.unidad == null },
            tipoFibra = R.string.error_fibra_requerida.takeIf { form.tipoFibra == null }
        )
        _erroresConsumo.value = errores
        if (errores.hayErrores) return null

        val consumo = ConsumoMateriaPrima(
            id = siguienteId++,
            ordenTrabajo = ordenesTrabajo[form.otIndex],
            lote = lotesDe(form.otIndex)[form.loteIndex],
            cantidad = parsearCantidad(form.cantidad) ?: return null,
            unidad = form.unidad ?: return null,
            tipoFibra = form.tipoFibra ?: return null,
            fechaHora = LocalDateTime.now(),
            operario = OPERARIO_SESION
        )
        _consumos.value = _consumos.value.orEmpty() + consumo
        actualizarAnalisis()
        consumoForm = ConsumoFormState()
        return consumo
    }

    private fun erroresConsumoActuales() = _erroresConsumo.value ?: ConsumoErrores()

    // =====================================================================
    // HU02 - Merma de producción
    // =====================================================================

    var mermaForm = MermaFormState()
        private set

    private val _erroresMerma = MutableLiveData(MermaErrores())
    val erroresMerma: LiveData<MermaErrores> = _erroresMerma

    fun onMermaOtSeleccionada(index: Int) {
        if (index == mermaForm.otIndex) return
        mermaForm = mermaForm.copy(otIndex = index, loteIndex = SIN_SELECCION)
        _erroresMerma.value = erroresMermaActuales().copy(ot = null)
    }

    fun onMermaLoteSeleccionado(index: Int) {
        if (index == mermaForm.loteIndex) return
        mermaForm = mermaForm.copy(loteIndex = index)
        _erroresMerma.value = erroresMermaActuales().copy(lote = null)
    }

    fun onMermaEtapaSeleccionada(index: Int) {
        val etapa = etapasProductivas.getOrNull(index)
        if (etapa == mermaForm.etapa) return
        mermaForm = mermaForm.copy(etapa = etapa)
        _erroresMerma.value = erroresMermaActuales().copy(etapa = null)
    }

    fun onMermaCantidadCambiada(texto: String) {
        if (texto == mermaForm.cantidad) return
        mermaForm = mermaForm.copy(cantidad = texto)
        _erroresMerma.value = erroresMermaActuales().copy(cantidad = null)
    }

    fun onMermaUnidadSeleccionada(unidad: UnidadMedida?) {
        if (unidad == mermaForm.unidad) return
        mermaForm = mermaForm.copy(unidad = unidad)
        _erroresMerma.value = erroresMermaActuales().copy(unidad = null)
    }

    /**
     * Valida el formulario de HU02. Si es válido, simula el guardado,
     * reinicia el formulario y devuelve el registro creado; si no, devuelve null.
     * Tras guardar, evalúa el umbral de merma del lote (HU16).
     */
    fun registrarMerma(): MermaProduccion? {
        val form = mermaForm
        val errores = MermaErrores(
            ot = R.string.error_ot_requerida.takeIf { form.otIndex == SIN_SELECCION },
            lote = R.string.error_lote_requerido.takeIf { form.loteIndex == SIN_SELECCION },
            etapa = R.string.error_etapa_requerida.takeIf { form.etapa == null },
            cantidad = validarCantidad(form.cantidad),
            unidad = R.string.error_unidad_requerida.takeIf { form.unidad == null }
        )
        _erroresMerma.value = errores
        if (errores.hayErrores) return null

        val merma = MermaProduccion(
            id = siguienteId++,
            ordenTrabajo = ordenesTrabajo[form.otIndex],
            lote = lotesDe(form.otIndex)[form.loteIndex],
            etapa = form.etapa ?: return null,
            cantidad = parsearCantidad(form.cantidad) ?: return null,
            unidad = form.unidad ?: return null,
            fechaHora = LocalDateTime.now(),
            operario = OPERARIO_SESION
        )
        _mermas.value = _mermas.value.orEmpty() + merma
        actualizarAnalisis()
        evaluarUmbralMerma(merma)
        mermaForm = MermaFormState()
        return merma
    }

    private fun erroresMermaActuales() = _erroresMerma.value ?: MermaErrores()

    // =====================================================================
    // HU03 - Consulta de mermas por lote
    // =====================================================================

    val configuracionPlanta = ConfiguracionPlanta()

    val umbralMerma: Double
        get() = configuracionPlanta.umbralMermaPorcentaje

    /** Todos los lotes analizables (consumo > 0 y merma en la misma unidad). */
    private val _resumenes = MutableLiveData<List<ResumenMermaLote>>(emptyList())

    /** Lotes disponibles para el selector de la consulta. */
    private val _lotesConsultables = MutableLiveData<List<String>>(emptyList())
    val lotesConsultables: LiveData<List<String>> = _lotesConsultables

    /** Lotes resultantes tras aplicar filtros y ordenación. */
    private val _resultadoConsulta = MutableLiveData<List<ResumenMermaLote>>(emptyList())
    val resultadoConsulta: LiveData<List<ResumenMermaLote>> = _resultadoConsulta

    /** Cantidad de lotes que hoy superan el umbral (banner de HU16). */
    private val _lotesSobreUmbral = MutableLiveData(0)
    val lotesSobreUmbral: LiveData<Int> = _lotesSobreUmbral

    var filtrosConsulta = FiltrosConsultaMerma()
        private set

    private val _errorRangoFechas = MutableLiveData<Int?>(null)
    val errorRangoFechas: LiveData<Int?> = _errorRangoFechas

    fun onFiltroLoteSeleccionado(lote: String?) {
        if (lote == filtrosConsulta.lote) return
        aplicarFiltros(filtrosConsulta.copy(lote = lote))
    }

    fun onFiltroFibraSeleccionada(fibra: TipoFibra?) {
        if (fibra == filtrosConsulta.tipoFibra) return
        aplicarFiltros(filtrosConsulta.copy(tipoFibra = fibra))
    }

    /** Rechaza la fecha si quedaría posterior a "Hasta". `null` quita el filtro. */
    fun onFechaDesdeSeleccionada(fecha: LocalDate?) {
        val hasta = filtrosConsulta.fechaHasta
        if (fecha != null && hasta != null && fecha.isAfter(hasta)) {
            _errorRangoFechas.value = R.string.error_rango_fechas
            return
        }
        _errorRangoFechas.value = null
        aplicarFiltros(filtrosConsulta.copy(fechaDesde = fecha))
    }

    /** Rechaza la fecha si quedaría anterior a "Desde". `null` quita el filtro. */
    fun onFechaHastaSeleccionada(fecha: LocalDate?) {
        val desde = filtrosConsulta.fechaDesde
        if (fecha != null && desde != null && fecha.isBefore(desde)) {
            _errorRangoFechas.value = R.string.error_rango_fechas
            return
        }
        _errorRangoFechas.value = null
        aplicarFiltros(filtrosConsulta.copy(fechaHasta = fecha))
    }

    fun onOrdenarPorPorcentajeCambiado(activo: Boolean) {
        if (activo == filtrosConsulta.ordenarPorPorcentaje) return
        aplicarFiltros(filtrosConsulta.copy(ordenarPorPorcentaje = activo))
    }

    fun limpiarFiltros() {
        _errorRangoFechas.value = null
        aplicarFiltros(FiltrosConsultaMerma())
    }

    private fun aplicarFiltros(filtros: FiltrosConsultaMerma) {
        filtrosConsulta = filtros
        _resultadoConsulta.value =
            AnalisisMerma.filtrarYOrdenar(_resumenes.value.orEmpty(), filtros)
    }

    /** Recalcula la consulta cada vez que cambian consumos o mermas. */
    private fun actualizarAnalisis() {
        val resumenes = AnalisisMerma.resumirPorLote(_consumos.value.orEmpty(), _mermas.value.orEmpty())
        _resumenes.value = resumenes
        _lotesConsultables.value = resumenes.map { it.lote }.distinct().sorted()
        _lotesSobreUmbral.value = resumenes.count { it.superaUmbral(umbralMerma) }
        aplicarFiltros(filtrosConsulta)
    }

    // =====================================================================
    // HU16 - Alerta por merma excesiva
    // =====================================================================

    private val _alertaMerma = MutableLiveData<Evento<AlertaMerma>>()
    val alertaMerma: LiveData<Evento<AlertaMerma>> = _alertaMerma

    /**
     * Si la merma recién registrada deja el lote por encima del umbral, emite
     * una alerta. Si queda dentro del umbral (o el lote aún no tiene consumo en
     * la misma unidad), la merma se conserva sin generar alerta.
     */
    private fun evaluarUmbralMerma(merma: MermaProduccion) {
        val resumenLote = _resumenes.value.orEmpty().find {
            it.ordenTrabajo == merma.ordenTrabajo && it.lote == merma.lote && it.unidad == merma.unidad
        }
        AnalisisMerma.evaluarAlerta(resumenLote, umbralMerma, merma.fechaHora)
            ?.let { _alertaMerma.value = Evento(it) }
    }

    init {
        actualizarAnalisis()
    }

    // ---------------------------------------------------------------------
    // Validaciones compartidas
    // ---------------------------------------------------------------------

    @StringRes
    private fun validarCantidad(texto: String): Int? = when {
        texto.isBlank() -> R.string.error_cantidad_requerida
        parsearCantidad(texto) == null -> R.string.error_cantidad_invalida
        else -> null
    }

    /** Acepta coma o punto decimal; devuelve null si no es un número > 0. */
    private fun parsearCantidad(texto: String): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 && it.isFinite() }

    companion object {
        const val SIN_SELECCION = -1

        /** Operario simulado hasta integrar el login real (Sprint 2). */
        private const val OPERARIO_SESION = "OP-001"

        /** Carga lotes de ejemplo para la demo del Sprint 1 (ver [DatosDemoProduccion]). */
        private const val CARGAR_DATOS_DEMO = true
    }
}
