package com.tejisur.app.produccion.ui

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tejisur.app.R
import com.tejisur.app.databinding.FragmentConsultaMermasBinding
import com.tejisur.app.produccion.model.ResumenMermaLote
import com.tejisur.app.produccion.model.TipoFibra
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * HU03 - Consultar mermas por lote (filtros por lote, fecha y fibra; orden por %).
 * HU16 - Muestra el banner y resalta los lotes que superan el umbral de Planta.
 */
class ConsultaMermasFragment : Fragment() {

    private var _binding: FragmentConsultaMermasBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val viewModel: ProduccionViewModel by activityViewModels()

    private lateinit var adapter: LoteMermaAdapter

    /** Lotes cargados en el Spinner (posición 0 = "Todos los lotes"). */
    private var lotesEnSpinner: List<String> = emptyList()

    private val fibras: List<TipoFibra> = TipoFibra.entries

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultaMermasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = LoteMermaAdapter(viewModel.umbralMerma)
        binding.rvLotesMerma.adapter = adapter

        configurarLeyenda()
        configurarAdaptadores()
        // 1) Pintar los filtros guardados en el ViewModel (sobreviven a la rotación)
        renderizarFiltros()
        // 2) Recién entonces escuchar cambios del usuario
        configurarListeners()
        observarDatos()
    }

    /** HU16: la leyenda usa el mismo umbral que la regla de alerta. */
    private fun configurarLeyenda() = with(binding) {
        tvLeyendaNormal.text = getString(R.string.consulta_leyenda_normal, viewModel.umbralMerma)
        tvLeyendaExcesiva.text = getString(R.string.consulta_leyenda_excesiva, viewModel.umbralMerma)
    }

    private fun configurarAdaptadores() {
        binding.spinnerFiltroFibra.adapter = requireContext().adaptadorConHint(
            getString(R.string.filtro_todas_fibras),
            fibras.map { getString(it.etiquetaRes) }
        )
        actualizarSpinnerLote(viewModel.lotesConsultables.value.orEmpty())
    }

    private fun configurarListeners() = with(binding) {
        spinnerFiltroLote.alSeleccionar { posicion ->
            viewModel.onFiltroLoteSeleccionado(lotesEnSpinner.getOrNull(posicion - 1))
        }

        spinnerFiltroFibra.alSeleccionar { posicion ->
            viewModel.onFiltroFibraSeleccionada(fibras.getOrNull(posicion - 1))
        }

        btnFechaDesde.setOnClickListener {
            elegirFecha(viewModel.filtrosConsulta.fechaDesde, viewModel::onFechaDesdeSeleccionada)
        }

        btnFechaHasta.setOnClickListener {
            elegirFecha(viewModel.filtrosConsulta.fechaHasta, viewModel::onFechaHastaSeleccionada)
        }

        swOrdenarPorcentaje.setOnCheckedChangeListener { _, activo ->
            viewModel.onOrdenarPorPorcentajeCambiado(activo)
        }

        btnLimpiarFiltros.setOnClickListener {
            viewModel.limpiarFiltros()
            renderizarFiltros()
        }
    }

    private fun observarDatos() {
        viewModel.lotesConsultables.observe(viewLifecycleOwner) { lotes ->
            if (lotes != lotesEnSpinner) actualizarSpinnerLote(lotes)
        }

        viewModel.resultadoConsulta.observe(viewLifecycleOwner) { lotes ->
            mostrarResultado(lotes)
        }

        viewModel.lotesSobreUmbral.observe(viewLifecycleOwner) { cantidad ->
            with(binding.tvBannerAlerta) {
                isVisible = cantidad > 0
                if (cantidad > 0) {
                    text = resources.getQuantityString(
                        R.plurals.consulta_banner_alerta, cantidad, cantidad, viewModel.umbralMerma
                    )
                }
            }
        }

        viewModel.errorRangoFechas.observe(viewLifecycleOwner) { error ->
            binding.tvErrorFechas.mostrarError(error)
        }
    }

    private fun mostrarResultado(lotes: List<ResumenMermaLote>) {
        // Al reordenar, volver al inicio para que se vea el lote con mayor %
        adapter.submitList(lotes) { _binding?.rvLotesMerma?.scrollToPosition(0) }

        with(binding) {
            tvResultados.text =
                resources.getQuantityString(R.plurals.consulta_resultados, lotes.size, lotes.size)

            val sinDatos = viewModel.lotesConsultables.value.isNullOrEmpty()
            tvSinRegistros.setText(
                if (sinDatos) R.string.consulta_sin_registros else R.string.consulta_sin_resultados
            )
            tvSinRegistros.isVisible = lotes.isEmpty()
            btnLimpiarFiltros.isEnabled = viewModel.filtrosConsulta.tieneFiltrosActivos
        }
    }

    /** Sincroniza los controles con [ProduccionViewModel.filtrosConsulta]. */
    private fun renderizarFiltros() {
        val filtros = viewModel.filtrosConsulta
        with(binding) {
            spinnerFiltroLote.setSelection(posicionLote(filtros.lote), false)
            val fibraIndex = filtros.tipoFibra?.let { fibras.indexOf(it) } ?: ProduccionViewModel.SIN_SELECCION
            spinnerFiltroFibra.setSelection(fibraIndex + 1, false)
            swOrdenarPorcentaje.isChecked = filtros.ordenarPorPorcentaje
            btnLimpiarFiltros.isEnabled = filtros.tieneFiltrosActivos
        }
        renderizarFechas()
    }

    private fun renderizarFechas() {
        val filtros = viewModel.filtrosConsulta
        with(binding) {
            btnFechaDesde.text = filtros.fechaDesde
                ?.let { getString(R.string.filtro_fecha_desde_valor, it.format(FORMATO_FECHA)) }
                ?: getString(R.string.filtro_fecha_desde)
            btnFechaHasta.text = filtros.fechaHasta
                ?.let { getString(R.string.filtro_fecha_hasta_valor, it.format(FORMATO_FECHA)) }
                ?: getString(R.string.filtro_fecha_hasta)
        }
    }

    /** Recarga los lotes disponibles y restaura el lote elegido. */
    private fun actualizarSpinnerLote(lotes: List<String>) {
        lotesEnSpinner = lotes
        with(binding.spinnerFiltroLote) {
            adapter = requireContext().adaptadorConHint(getString(R.string.filtro_todos_lotes), lotes)
            setSelection(posicionLote(viewModel.filtrosConsulta.lote), false)
        }
    }

    private fun posicionLote(lote: String?): Int =
        (lote?.let { lotesEnSpinner.indexOf(it) } ?: ProduccionViewModel.SIN_SELECCION) + 1

    /**
     * Abre un selector de fecha. "Quitar fecha" elimina ese extremo del rango.
     * No se permiten fechas futuras porque no puede haber mermas registradas.
     */
    private fun elegirFecha(actual: LocalDate?, alElegir: (LocalDate?) -> Unit) {
        val inicial = actual ?: LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, anio, mes, dia ->
                alElegir(LocalDate.of(anio, mes + 1, dia))
                if (_binding != null) renderizarFechas()
            },
            inicial.year,
            inicial.monthValue - 1,
            inicial.dayOfMonth
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.filtro_quitar_fecha)) { _, _ ->
                alElegir(null)
                if (_binding != null) renderizarFechas()
            }
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvLotesMerma.adapter = null
        _binding = null
    }

    private companion object {
        val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
}
