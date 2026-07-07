package kurou.kodriver.domain.model

data class LmuWindowsTelemetryData(
    val timestampMs: Long,
    val engine: LmuWindowsEngineData,
    val inputs: LmuWindowsInputsData,
    val tyres: LmuWindowsTyreData,
    val fuel: LmuWindowsFuelData,
    val timing: LmuWindowsTimingData,
    val vehicle: LmuWindowsVehicleData,
)
