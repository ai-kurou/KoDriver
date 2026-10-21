package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.model.CelsiusReading

internal fun fakeGt7Ps5TelemetryData(
    lapCount: Int = 0,
    carCategory: String = "",
    tyreTemperature: Gt7Ps5TyreTemperatureData =
        Gt7Ps5TyreTemperatureData(
            CelsiusReading(0f),
            CelsiusReading(0f),
            CelsiusReading(0f),
            CelsiusReading(0f),
        ),
) = Gt7Ps5TelemetryData(
    lapCount = lapCount,
    lapsInRace = 0,
    bestLapTimeMs = -1,
    gasLevel = Gt7Ps5FuelUnit(0f),
    gasCapacity = Gt7Ps5FuelUnit(100f),
    carCategory = carCategory,
    tyreTemperature = tyreTemperature,
)
