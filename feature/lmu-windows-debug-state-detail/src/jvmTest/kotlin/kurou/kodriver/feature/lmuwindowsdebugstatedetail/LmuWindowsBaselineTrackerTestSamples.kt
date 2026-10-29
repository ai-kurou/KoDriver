package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsInputsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex

internal fun sampleLmuBaselineTelemetry(
    currentLap: Int,
    wheels: Map<WheelIndex, Double> = emptyMap(),
) = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres =
        LmuWindowsTyreData(
            wheels =
                wheels.mapValues { (_, wear) ->
                    LmuWindowsTyreWheelData(
                        surfaceTemperature = CelsiusReading(0f),
                        carcassTemperature = CelsiusReading(0f),
                        brakeTemperature = CelsiusReading(0f),
                        pressureKpa = PressureKpa(0.0),
                        wear = LmuWindowsTyreWearRatio(wear),
                    )
                },
        ),
    fuel = LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(0.0), capacityLiters = LmuWindowsFuelUnit(0.0)),
    timing =
        LmuWindowsTimingData(
            currentLapTimeMs = 0L,
            lastLapTimeMs = 0L,
            bestLapTimeMs = 0L,
            sector1Ms = 0L,
            sector1And2Ms = 0L,
            currentLap = currentLap,
            maxLaps = 0,
        ),
    vehicle =
        LmuWindowsVehicleData(
            localVelocityX = 0.0,
            localVelocityY = 0.0,
            localVelocityZ = 0.0,
            positionX = 0.0,
            positionY = 0.0,
            positionZ = 0.0,
        ),
)
