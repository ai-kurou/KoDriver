package kurou.kodriver.domain.telemetrylog.usecase

import kurou.kodriver.domain.model.ReadoutItemKey

internal enum class TestTelemetryReadoutItemKey(
    override val value: String,
) : ReadoutItemKey {
    Flag("flag"),
    RemainingFuelLaps("remaining_fuel_laps"),
}
