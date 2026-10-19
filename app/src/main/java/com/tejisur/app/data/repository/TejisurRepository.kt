package com.tejisur.app.data.repository

import com.tejisur.app.data.model.BatchQueryItem
import com.tejisur.app.data.model.ConsumptionRecord
import com.tejisur.app.data.model.UserSession
import com.tejisur.app.data.model.WasteRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TejisurRepository {

    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private val _consumptions = MutableStateFlow<List<ConsumptionRecord>>(
        listOf(
            ConsumptionRecord(
                id = "TX-892401",
                orderId = "OT-001",
                lotId = "LOT-001",
                fiberType = "Algodón Pima",
                fiberDetail = "Grado Extralargo",
                amount = 25.5,
                unit = "kg",
                operatorName = "Juan Pérez",
                timestamp = "14/10/2024 - 08:35 AM",
                status = "Registrado",
                traceabilityId = "TS-PT-2024-8841-A"
            ),
            ConsumptionRecord(
                id = "TX-892398",
                orderId = "OT-002",
                lotId = "LOT-002",
                fiberType = "Alpaca",
                fiberDetail = "Baby Alpaca 21.5u",
                amount = 50.0,
                unit = "kg",
                operatorName = "Juan Pérez",
                timestamp = "14/10/2024 - 07:15 AM",
                status = "Registrado",
                traceabilityId = "TS-PT-2024-8839-B"
            ),
            ConsumptionRecord(
                id = "TX-892340",
                orderId = "OT-001",
                lotId = "LOT-003",
                fiberType = "Algodón Pima",
                fiberDetail = "Grado Extralargo",
                amount = 50.0,
                unit = "kg",
                operatorName = "Juan Pérez",
                timestamp = "13/10/2024 - 16:40 PM",
                status = "Registrado",
                traceabilityId = "TS-PT-2024-8790-C"
            )
        )
    )
    val consumptions: StateFlow<List<ConsumptionRecord>> = _consumptions.asStateFlow()

    private val _wastes = MutableStateFlow<List<WasteRecord>>(
        listOf(
            WasteRecord(
                id = "#REG-88219",
                orderId = "OT-001",
                lotId = "LOT-001",
                stage = "Corte",
                unit = "kg",
                amount = 4.5,
                wastePercent = 2.1,
                operatorName = "Juan Pérez",
                scale = "Báscula B-04 • Calibrada",
                timestamp = "14/10/2024 • 10:15 AM",
                traceabilityLinked = true
            )
        )
    )
    val wastes: StateFlow<List<WasteRecord>> = _wastes.asStateFlow()

    private val _batches = MutableStateFlow<List<BatchQueryItem>>(
        listOf(
            BatchQueryItem(
                lotId = "LOT-001",
                orderId = "OT-001",
                fiber = "Algodón Pima",
                consumptionKg = 100.0,
                wasteKg = 6.0,
                wastePercent = 6.0,
                isExceeded = true,
                timestamp = "14/10/24 · 10:20",
                machine = "T-01 • Telar 04",
                statusText = "Supera umbral (5%)"
            ),
            BatchQueryItem(
                lotId = "LOT-002",
                orderId = "OT-002",
                fiber = "Algodón Pima",
                consumptionKg = 100.0,
                wasteKg = 3.0,
                wastePercent = 3.0,
                isExceeded = false,
                timestamp = "14/10/24 · 09:15",
                machine = "T-02 • Telar 02",
                statusText = "Conforme (≤5%)"
            ),
            BatchQueryItem(
                lotId = "LOT-003",
                orderId = "OT-003",
                fiber = "Alpaca",
                consumptionKg = 80.0,
                wasteKg = 2.0,
                wastePercent = 2.5,
                isExceeded = false,
                timestamp = "13/10/24 · 17:30",
                machine = "T-01 • Telar 01",
                statusText = "Conforme (≤5%)"
            )
        )
    )
    val batches: StateFlow<List<BatchQueryItem>> = _batches.asStateFlow()

    fun login(username: String, pass: String): Boolean {
        if (username.trim().equals("operario01", ignoreCase = true) && pass.isNotBlank()) {
            _currentUser.value = UserSession(
                username = username,
                fullName = "Juan Pérez",
                role = "Operario de producción",
                line = "Telar Jacquard #4",
                shift = "Turno 1 - Planta Sur"
            )
            return true
        }
        return false
    }

    fun logout() {
        _currentUser.value = null
    }

    fun addConsumption(
        orderId: String,
        lotId: String,
        fiberType: String,
        fiberDetail: String,
        amount: Double,
        unit: String
    ): ConsumptionRecord {
        val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val txNum = (892402..899999).random()
        val traceNum = (8842..9999).random()

        val record = ConsumptionRecord(
            id = "TX-$txNum",
            orderId = orderId,
            lotId = lotId,
            fiberType = fiberType,
            fiberDetail = fiberDetail,
            amount = amount,
            unit = unit,
            operatorName = _currentUser.value?.fullName ?: "Juan Pérez",
            timestamp = timestamp,
            status = "Registrado",
            traceabilityId = "TS-PT-2024-$traceNum-A"
        )
        _consumptions.value = listOf(record) + _consumptions.value
        return record
    }

    fun addWaste(
        orderId: String,
        lotId: String,
        stage: String,
        amount: Double,
        unit: String
    ): WasteRecord {
        val sdf = SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val regNum = (88220..88999).random()
        val percent = String.format(Locale.US, "%.1f", (amount / 100.0) * 100).toDoubleOrNull() ?: 2.1

        val record = WasteRecord(
            id = "#REG-$regNum",
            orderId = orderId,
            lotId = lotId,
            stage = stage,
            unit = unit,
            amount = amount,
            wastePercent = percent,
            operatorName = _currentUser.value?.fullName ?: "Juan Pérez",
            scale = "Báscula B-04 • Calibrada",
            timestamp = timestamp,
            traceabilityLinked = true
        )
        _wastes.value = listOf(record) + _wastes.value

        // Also update batch items
        val isExceeded = percent > 5.0
        val newBatch = BatchQueryItem(
            lotId = lotId,
            orderId = orderId,
            fiber = "Algodón Pima",
            consumptionKg = 100.0,
            wasteKg = amount,
            wastePercent = percent,
            isExceeded = isExceeded,
            timestamp = timestamp,
            machine = "T-01 • Telar 04",
            statusText = if (isExceeded) "Supera umbral (5%)" else "Conforme (≤5%)"
        )
        _batches.value = listOf(newBatch) + _batches.value.filter { it.lotId != lotId }

        return record
    }

    fun getBatch(lotId: String): BatchQueryItem? {
        return _batches.value.find { it.lotId == lotId } ?: _batches.value.firstOrNull()
    }
}
