package com.tejisur.app.data.model

data class UserSession(
    val id: String = "OP-449",
    val username: String = "operario01",
    val fullName: String = "Juan Pérez",
    val role: String = "Operario de producción",
    val line: String = "Telar Jacquard #4",
    val shift: String = "Turno 1 - Planta Sur"
)

enum class FiberType(val displayName: String, val detail: String) {
    ALGODON_PIMA("Algodón Pima", "Grado Extralargo"),
    ALPACA("Alpaca", "Baby Alpaca 21.5u"),
    POLIESTER("Poliéster", "Microfibra 150D")
}

enum class ProductionStage(val displayName: String) {
    CORTE("Corte"),
    CONFECCION("Confección")
}

data class ConsumptionRecord(
    val id: String,
    val orderId: String,
    val lotId: String,
    val fiberType: String,
    val fiberDetail: String,
    val amount: Double,
    val unit: String = "kg",
    val operatorName: String = "Juan Pérez",
    val timestamp: String,
    val status: String = "Registrado",
    val traceabilityId: String
)

data class WasteRecord(
    val id: String,
    val orderId: String,
    val lotId: String,
    val stage: String,
    val unit: String = "kg",
    val amount: Double,
    val wastePercent: Double,
    val operatorName: String = "Juan Pérez",
    val scale: String = "Báscula B-04 • Calibrada",
    val timestamp: String,
    val traceabilityLinked: Boolean = true
)

data class BatchQueryItem(
    val lotId: String,
    val orderId: String,
    val fiber: String,
    val consumptionKg: Double,
    val wasteKg: Double,
    val wastePercent: Double,
    val isExceeded: Boolean, // > 5.0%
    val timestamp: String,
    val machine: String = "T-01 • Telar 04",
    val statusText: String
)
