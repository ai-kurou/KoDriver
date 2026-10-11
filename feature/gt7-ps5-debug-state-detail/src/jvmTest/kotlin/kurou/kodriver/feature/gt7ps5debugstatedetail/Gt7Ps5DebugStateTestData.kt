package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData

internal fun sampleGt7Ps5Telemetry(
    lapCount: Int = 3,
    bestLapTimeMs: Int = 90_000,
    gasLevel: Float = 40f,
    gasCapacity: Float = 70f,
): Gt7Ps5TelemetryData =
    Gt7Ps5TelemetryData(
        lapCount = lapCount,
        lapsInRace = 10,
        bestLapTimeMs = bestLapTimeMs,
        gasLevel = Gt7Ps5FuelUnit(gasLevel),
        gasCapacity = Gt7Ps5FuelUnit(gasCapacity),
        tyreTemperature =
            Gt7Ps5TyreTemperatureData(
                CelsiusReading(69f),
                CelsiusReading(85f),
                CelsiusReading(100f),
                CelsiusReading(110f),
            ),
    )
