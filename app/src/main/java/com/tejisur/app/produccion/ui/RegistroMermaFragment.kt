package com.tejisur.app.produccion.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tejisur.app.R
import com.tejisur.app.databinding.FragmentRegistroMermaBinding
import com.tejisur.app.produccion.model.MermaProduccion
import com.tejisur.app.produccion.model.UnidadMedida

/** HU02 - Registrar merma de producción. */
class RegistroMermaFragment : Fragment() {

    private var _binding: FragmentRegistroMermaBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val viewModel: ProduccionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroMermaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarAdaptadoresFijos()

        // 1) Pintar el estado guardado en el ViewModel (sobrevive a la rotación)
        renderizarFormulario()
        // 2) Recién entonces escuchar cambios del usuario
        configurarListeners()
        observarErrores()
    }

    private fun configurarAdaptadoresFijos() = with(binding) {
        spinnerOt.adapter = requireContext()
            .adaptadorConHint(getString(R.string.spinner_hint_ot), viewModel.ordenesTrabajo)

        spinnerEtapa.adapter = requireContext().adaptadorConHint(
            getString(R.string.spinner_hint_etapa),
            viewModel.etapasProductivas.map { getString(it.etiquetaRes) }
        )
    }

    private fun configurarListeners() = with(binding) {
        spinnerOt.alSeleccionar { posicion ->
            val otIndex = posicion - 1
            if (otIndex != viewModel.mermaForm.otIndex) {
                viewModel.onMermaOtSeleccionada(otIndex)
                actualizarSpinnerLote()
            }
        }

        spinnerLote.alSeleccionar { posicion ->
            viewModel.onMermaLoteSeleccionado(posicion - 1)
        }

        spinnerEtapa.alSeleccionar { posicion ->
            viewModel.onMermaEtapaSeleccionada(posicion - 1)
        }

        etCantidadMerma.doAfterTextChanged { texto ->
            viewModel.onMermaCantidadCambiada(texto?.toString().orEmpty())
        }

        rgUnidad.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onMermaUnidadSeleccionada(
                when (checkedId) {
                    R.id.rbKg -> UnidadMedida.KG
                    R.id.rbMetros -> UnidadMedida.METROS
                    else -> null
                }
            )
        }

        btnRegistrar.setOnClickListener {
            val merma = viewModel.registrarMerma()
            if (merma != null) {
                mostrarExito(merma)
                renderizarFormulario() // El ViewModel ya reinició el formulario
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.error_formulario_incompleto,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observarErrores() {
        viewModel.erroresMerma.observe(viewLifecycleOwner) { errores ->
            with(binding) {
                tvErrorOt.mostrarError(errores.ot)
                tvErrorLote.mostrarError(errores.lote)
                tvErrorEtapa.mostrarError(errores.etapa)
                etCantidadMerma.error = errores.cantidad?.let { getString(it) }
                tvErrorUnidad.mostrarError(errores.unidad)
            }
        }
    }

    /** Sincroniza la vista con [ProduccionViewModel.mermaForm]. */
    private fun renderizarFormulario() {
        val form = viewModel.mermaForm
        with(binding) {
            spinnerOt.setSelection(form.otIndex + 1, false)
            actualizarSpinnerLote()

            val etapaIndex = form.etapa?.let { viewModel.etapasProductivas.indexOf(it) }
                ?: ProduccionViewModel.SIN_SELECCION
            spinnerEtapa.setSelection(etapaIndex + 1, false)

            if (etCantidadMerma.text.toString() != form.cantidad) {
                etCantidadMerma.setText(form.cantidad)
            }

            when (form.unidad) {
                UnidadMedida.KG -> rgUnidad.check(R.id.rbKg)
                UnidadMedida.METROS -> rgUnidad.check(R.id.rbMetros)
                null -> rgUnidad.clearCheck()
            }
        }
    }

    /** Recarga los lotes de la OT seleccionada y restaura el lote elegido. */
    private fun actualizarSpinnerLote() {
        val form = viewModel.mermaForm
        with(binding.spinnerLote) {
            adapter = requireContext().adaptadorConHint(
                getString(R.string.spinner_hint_lote),
                viewModel.lotesDe(form.otIndex)
            )
            setSelection(form.loteIndex + 1, false)
            isEnabled = form.otIndex != ProduccionViewModel.SIN_SELECCION
        }
    }

    private fun mostrarExito(merma: MermaProduccion) {
        val mensaje = getString(
            R.string.merma_registrada_exito,
            merma.cantidad.toString(),
            merma.unidad.simbolo,
            getString(merma.etapa.etiquetaRes),
            merma.ordenTrabajo,
            merma.lote
        )
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
