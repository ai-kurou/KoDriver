package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState

internal fun fakeGt7Ps5TelemetryData(
    lapCount: Int = 0,
    carCategory: String = "",
    tyreTemperature: Gt7Ps5TyreTemperatureData = Gt7Ps5TyreTemperatureData(0f, 0f, 0f, 0f),
) = Gt7Ps5TelemetryData(
    lapCount = lapCount,
    lapsInRace = 0,
    bestLapTimeMs = -1,
    gasLevel = 0f,
    gasCapacity = 100f,
    carCategory = carCategory,
    tyreTemperature = tyreTemperature,
)

internal fun fakeRaceFlagsData(
    gamePhase: SessionPhase = SessionPhase.GARAGE,
    yellowFlagState: SessionYellowFlagState = SessionYellowFlagState.NONE,
    playerFlag: PrimaryFlag = PrimaryFlag.GREEN,
) = LmuWindowsRaceFlagsData(
    gamePhase = gamePhase,
    yellowFlagState = yellowFlagState,
    sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.CLEAR, SectorFlagState.CLEAR),
    startLight = 0,
    numRedLights = 0,
    playerFlag = playerFlag,
    playerUnderYellow = false,
    playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
)

internal fun fakeLmuWindowsTelemetryData(speedX: Double = 0.0) =
    LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing =
            LmuWindowsTimingData(
                currentLapTimeMs = 0L,
                lastLapTimeMs = 0L,
                bestLapTimeMs = 0L,
                sector1Ms = 0L,
                sector1And2Ms = 0L,
                currentLap = 0,
                maxLaps = 0,
            ),
        vehicle =
            LmuWindowsVehicleData(
                localVelocityX = speedX,
                localVelocityY = 0.0,
                localVelocityZ = 0.0,
                positionX = 0.0,
                positionY = 0.0,
                positionZ = 0.0,
            ),
    )
