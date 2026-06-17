package kurou.kodriver.domain.model

data class LmuWindowsTelemetryData(
    val timestampMs: Long,
    val engine: EngineData,
    val inputs: InputsData,
    val tyres: TyreData,
    val fuel: FuelData,
    val timing: TimingData,
    val vehicle: VehicleData,
)
