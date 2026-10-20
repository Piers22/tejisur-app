package com.tejisur.app.produccion.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tejisur.app.R
import com.tejisur.app.databinding.ItemLoteMermaBinding
import com.tejisur.app.produccion.model.ResumenMermaLote
import java.time.format.DateTimeFormatter

/**
 * HU03 - Lista de lotes con su merma.
 * HU16 - Resalta en rojo los lotes cuyo porcentaje supera [umbral].
 */
class LoteMermaAdapter(
    private val umbral: Double
) : ListAdapter<ResumenMermaLote, LoteMermaAdapter.LoteViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoteViewHolder =
        LoteViewHolder(
            ItemLoteMermaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: LoteViewHolder, position: Int) =
        holder.bind(getItem(position), umbral)

    class LoteViewHolder(
        private val binding: ItemLoteMermaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(resumen: ResumenMermaLote, umbral: Double) = with(binding) {
            val contexto = root.context
            val simbolo = resumen.unidad.simbolo

            tvOrdenTrabajo.text = resumen.ordenTrabajo
            tvLote.text = resumen.lote
            tvConsumo.text = contexto.getString(R.string.item_formato_cantidad, resumen.consumoTotal, simbolo)
            tvMerma.text = contexto.getString(R.string.item_formato_cantidad, resumen.mermaTotal, simbolo)
            tvPorcentajeMerma.text = contexto.getString(R.string.item_formato_porcentaje, resumen.porcentajeMerma)
            tvDetalleLote.text = contexto.getString(
                R.string.item_detalle_lote,
                contexto.getString(resumen.tipoFibra.etiquetaRes),
                resumen.fecha.format(FORMATO_FECHA)
            )

            // HU16: el color se reasigna siempre porque las filas se reciclan
            val excesiva = resumen.superaUmbral(umbral)
            val colorEstado = ContextCompat.getColor(
                contexto, if (excesiva) R.color.merma_excesiva else R.color.merma_normal
            )
            val colorFondo = ContextCompat.getColor(
                contexto, if (excesiva) R.color.merma_excesiva_bg else R.color.merma_normal_bg
            )

            viewIndicadorMerma.setBackgroundColor(colorEstado)
            clItemLoteMerma.backgroundTintList =
                if (excesiva) ColorStateList.valueOf(colorFondo) else null
            tvPorcentajeMerma.setTextColor(colorEstado)
            tvPorcentajeMerma.backgroundTintList = ColorStateList.valueOf(colorFondo)
            tvEstadoMerma.setTextColor(colorEstado)
            tvEstadoMerma.text = if (excesiva) {
                contexto.getString(R.string.item_estado_alerta, umbral)
            } else {
                contexto.getString(R.string.item_estado_normal)
            }
        }
    }

    private companion object {
        val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val DIFF = object : DiffUtil.ItemCallback<ResumenMermaLote>() {
            override fun areItemsTheSame(a: ResumenMermaLote, b: ResumenMermaLote) =
                a.ordenTrabajo == b.ordenTrabajo && a.lote == b.lote && a.unidad == b.unidad

            override fun areContentsTheSame(a: ResumenMermaLote, b: ResumenMermaLote) = a == b
        }
    }
}
