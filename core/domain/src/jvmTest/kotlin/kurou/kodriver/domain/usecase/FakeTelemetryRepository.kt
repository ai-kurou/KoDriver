package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.EngineData
import kurou.kodriver.domain.model.FuelData
import kurou.kodriver.domain.model.InputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.TimingData
import kurou.kodriver.domain.model.TyreData
import kurou.kodriver.domain.model.VehicleData
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
    engine = EngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = InputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = TyreData(wheels = emptyMap()),
    fuel = FuelData(currentLiters = 0.0, capacityLiters = 0.0),
    timing = TimingData(
        currentLapTimeMs = 0L, lastLapTimeMs = 0L, bestLapTimeMs = 0L,
        sector1Ms = 0L, sector2Ms = 0L, currentLap = 0, maxLaps = 0,
    ),
    vehicle = VehicleData(
        localVelocityX = speedX, localVelocityY = 0.0, localVelocityZ = 0.0,
        positionX = 0.0, positionY = 0.0, positionZ = 0.0,
    ),
)
