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
import com.tejisur.app.databinding.FragmentRegistroConsumoBinding
import com.tejisur.app.produccion.model.ConsumoMateriaPrima
import com.tejisur.app.produccion.model.TipoFibra
import com.tejisur.app.produccion.model.UnidadMedida

/** HU01 - Registrar consumo de materia prima. */
class RegistroConsumoFragment : Fragment() {

    private var _binding: FragmentRegistroConsumoBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val viewModel: ProduccionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroConsumoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.spinnerOt.adapter = requireContext()
            .adaptadorConHint(getString(R.string.spinner_hint_ot), viewModel.ordenesTrabajo)

        // 1) Pintar el estado guardado en el ViewModel (sobrevive a la rotación)
        renderizarFormulario()
        // 2) Recién entonces escuchar cambios del usuario
        configurarListeners()
        observarErrores()
    }

    private fun configurarListeners() = with(binding) {
        spinnerOt.alSeleccionar { posicion ->
            val otIndex = posicion - 1
            if (otIndex != viewModel.consumoForm.otIndex) {
                viewModel.onConsumoOtSeleccionada(otIndex)
                actualizarSpinnerLote()
            }
        }

        spinnerLote.alSeleccionar { posicion ->
            viewModel.onConsumoLoteSeleccionado(posicion - 1)
        }

        etCantidad.doAfterTextChanged { texto ->
            viewModel.onConsumoCantidadCambiada(texto?.toString().orEmpty())
        }

        rgUnidad.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onConsumoUnidadSeleccionada(
                when (checkedId) {
                    R.id.rbKg -> UnidadMedida.KG
                    R.id.rbMetros -> UnidadMedida.METROS
                    else -> null
                }
            )
        }

        rgTipoFibra.setOnCheckedChangeListener { _, checkedId ->
            viewModel.onConsumoFibraSeleccionada(
                when (checkedId) {
                    R.id.rbPima -> TipoFibra.PIMA
                    R.id.rbAlpaca -> TipoFibra.ALPACA
                    else -> null
                }
            )
        }

        btnRegistrar.setOnClickListener {
            val consumo = viewModel.registrarConsumo()
            if (consumo != null) {
                mostrarExito(consumo)
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
        viewModel.erroresConsumo.observe(viewLifecycleOwner) { errores ->
            with(binding) {
                tvErrorOt.mostrarError(errores.ot)
                tvErrorLote.mostrarError(errores.lote)
                etCantidad.error = errores.cantidad?.let { getString(it) }
                tvErrorUnidad.mostrarError(errores.unidad)
                tvErrorFibra.mostrarError(errores.tipoFibra)
            }
        }
    }

    /** Sincroniza la vista con [ProduccionViewModel.consumoForm]. */
    private fun renderizarFormulario() {
        val form = viewModel.consumoForm
        with(binding) {
            spinnerOt.setSelection(form.otIndex + 1, false)
            actualizarSpinnerLote()

            if (etCantidad.text.toString() != form.cantidad) {
                etCantidad.setText(form.cantidad)
            }

            when (form.unidad) {
                UnidadMedida.KG -> rgUnidad.check(R.id.rbKg)
                UnidadMedida.METROS -> rgUnidad.check(R.id.rbMetros)
                null -> rgUnidad.clearCheck()
            }

            when (form.tipoFibra) {
                TipoFibra.PIMA -> rgTipoFibra.check(R.id.rbPima)
                TipoFibra.ALPACA -> rgTipoFibra.check(R.id.rbAlpaca)
                null -> rgTipoFibra.clearCheck()
            }
        }
    }

    /** Recarga los lotes de la OT seleccionada y restaura el lote elegido. */
    private fun actualizarSpinnerLote() {
        val form = viewModel.consumoForm
        with(binding.spinnerLote) {
            adapter = requireContext().adaptadorConHint(
                getString(R.string.spinner_hint_lote),
                viewModel.lotesDe(form.otIndex)
            )
            setSelection(form.loteIndex + 1, false)
            isEnabled = form.otIndex != ProduccionViewModel.SIN_SELECCION
        }
    }

    private fun mostrarExito(consumo: ConsumoMateriaPrima) {
        val mensaje = getString(
            R.string.consumo_registrado_exito,
            consumo.cantidad.toString(),
            consumo.unidad.simbolo,
            getString(consumo.tipoFibra.etiquetaRes),
            consumo.ordenTrabajo,
            consumo.lote
        )
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
