package kurou.kodriver.domain.model

data class TelemetryData(
    val timestampMs: Long,
    val engine: EngineData,
    val inputs: InputsData,
    val tyres: TyreData,
    val fuel: FuelData,
    val timing: TimingData,
    val vehicle: VehicleData,
)
