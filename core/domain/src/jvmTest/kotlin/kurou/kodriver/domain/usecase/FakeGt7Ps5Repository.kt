package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.repository.Gt7Ps5Repository

internal class FakeGt7Ps5Repository(
    private val stream: Flow<Gt7Ps5TelemetryData> = flowOf(),
) : Gt7Ps5Repository {
    override fun telemetryStream(): Flow<Gt7Ps5TelemetryData> = stream
}

internal fun fakeGt7Ps5TelemetryData(lapCount: Int = 0) = Gt7Ps5TelemetryData(
    lapCount = lapCount,
    lapsInRace = 0,
    bestLapTimeMs = -1,
    gasLevel = 0f,
    gasCapacity = 100f,
    energyRecovery = 0f,
)
