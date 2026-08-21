package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuFuelConsumptionCalculatorTest {
    @Test
    fun `LMU 5周消費後の残量50パーセントなら1周あたり10パーセント消費で残り5周`() {
        val result =
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
                telemetry = sampleLmuTelemetry(currentLap = 5),
            )

        assertEquals(10.0, result?.consumptionPerLap)
        assertEquals(5, result?.remainingLaps)
        assertEquals(5.0, result?.preciseRemainingLaps)
    }

    @Test
    fun `LMU virtualEnergyがnullの場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(virtualEnergy = null, telemetry = sampleLmuTelemetry(currentLap = 5)),
        )
    }

    @Test
    fun `LMU telemetryがnullの場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
                telemetry = null,
            ),
        )
    }

    @Test
    fun `LMU currentLapが0以下の場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
                telemetry = sampleLmuTelemetry(currentLap = 0),
            ),
        )
    }

    @Test
    fun `LMU 消費量が0以下（残量が減っていない）場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(1.0)),
                telemetry = sampleLmuTelemetry(currentLap = 3),
            ),
        )
    }

    @Test
    fun `LMU バーチャルエナジー残量を割合で返す`() {
        assertEquals(
            50.0,
            calculateLmuVirtualEnergyRemainingPercent(
                LmuWindowsVirtualEnergyData(remainingRatio = LmuWindowsVirtualEnergyRatio(0.5)),
            ),
        )
    }

    @Test
    fun `LMU virtualEnergyがnullの場合は残量割合を返さない`() {
        assertNull(calculateLmuVirtualEnergyRemainingPercent(null))
    }

    @Test
    fun `タイヤ摩耗 5周で最も摩耗した輪の残溝が50パーセントなら1周あたり10パーセント摩耗で残り5周`() {
        val result =
            calculateLmuTyreWearRemainingLaps(
                sampleLmuTelemetry(
                    currentLap = 5,
                    wheels = mapOf(WheelIndex.FRONT_LEFT to 0.5, WheelIndex.FRONT_RIGHT to 0.8),
                ),
            )

        assertEquals(5.0, result)
    }

    @Test
    fun `タイヤ摩耗 telemetryがnullの場合はnullを返す`() {
        assertNull(calculateLmuTyreWearRemainingLaps(null))
    }

    @Test
    fun `タイヤ摩耗 currentLapが0以下の場合はnullを返す`() {
        assertNull(
            calculateLmuTyreWearRemainingLaps(
                sampleLmuTelemetry(currentLap = 0, wheels = mapOf(WheelIndex.FRONT_LEFT to 0.5)),
            ),
        )
    }

    @Test
    fun `タイヤ摩耗 wheelsが空の場合はnullを返す`() {
        assertNull(calculateLmuTyreWearRemainingLaps(sampleLmuTelemetry(currentLap = 5, wheels = emptyMap())))
    }

    @Test
    fun `タイヤ摩耗 摩耗量が0以下（残溝が減っていない）場合はnullを返す`() {
        assertNull(
            calculateLmuTyreWearRemainingLaps(
                sampleLmuTelemetry(currentLap = 3, wheels = mapOf(WheelIndex.FRONT_LEFT to 1.0)),
            ),
        )
    }

    private fun sampleLmuTelemetry(
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
}
