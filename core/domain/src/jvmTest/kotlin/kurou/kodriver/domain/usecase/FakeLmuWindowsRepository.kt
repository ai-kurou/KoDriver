package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.repository.LmuWindowsRepository

internal class FakeLmuWindowsRepository(
    private val connected: Boolean = true,
    private val stream: Flow<LmuWindowsTelemetryData> = flowOf(),
) : LmuWindowsRepository {
    var disconnectCalled = false

    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = stream
    override suspend fun isConnected(): Boolean = connected
    override suspend fun disconnect() { disconnectCalled = true }
}

internal fun fakeLmuWindowsTelemetryData(speedX: Double = 0.0) = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = LmuWindowsTyreData(wheels = emptyMap()),
    fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
    timing = LmuWindowsTimingData(
        currentLapTimeMs = 0L, lastLapTimeMs = 0L, bestLapTimeMs = 0L,
        sector1Ms = 0L, sector2Ms = 0L, currentLap = 0, maxLaps = 0,
    ),
    vehicle = LmuWindowsVehicleData(
        localVelocityX = speedX, localVelocityY = 0.0, localVelocityZ = 0.0,
        positionX = 0.0, positionY = 0.0, positionZ = 0.0,
    ),
)
