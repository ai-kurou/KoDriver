package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FuelConsumptionCalculatorTest {
    @Test
    fun `LMU 5周消費後の残量50パーセントなら1周あたり10パーセント消費で残り5周`() {
        val result =
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5),
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
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5),
                telemetry = null,
            ),
        )
    }

    @Test
    fun `LMU currentLapが0以下の場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5),
                telemetry = sampleLmuTelemetry(currentLap = 0),
            ),
        )
    }

    @Test
    fun `LMU 消費量が0以下（残量が減っていない）場合はnullを返す`() {
        assertNull(
            calculateLmuVirtualEnergyConsumption(
                virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 1.0),
                telemetry = sampleLmuTelemetry(currentLap = 3),
            ),
        )
    }

    @Test
    fun `GT7 3周で30L消費し残り40Lなら1周あたり10Lで残り4周`() {
        val result =
            calculateGt7FuelConsumption(
                sampleGt7Telemetry(lapCount = 3, gasLevel = 40f, gasCapacity = 70f),
            )

        assertEquals(10.0, result?.consumptionPerLap)
        assertEquals(4, result?.remainingLaps)
        assertEquals(4.0, result?.preciseRemainingLaps)
    }

    @Test
    fun `GT7 燃料残量を割合で返す`() {
        assertEquals(
            25.0,
            calculateGt7FuelRemainingPercent(sampleGt7Telemetry(lapCount = 0, gasLevel = 17.5f, gasCapacity = 70f)),
        )
    }

    @Test
    fun `GT7 燃料容量が0以下の場合は残量割合を返さない`() {
        assertNull(
            calculateGt7FuelRemainingPercent(
                sampleGt7Telemetry(lapCount = 0, gasLevel = 17.5f, gasCapacity = 0f),
            ),
        )
    }

    @Test
    fun `GT7 telemetryがnullの場合は残量割合を返さない`() {
        assertNull(calculateGt7FuelRemainingPercent(null))
    }

    @Test
    fun `GT7 telemetryがnullの場合はnullを返す`() {
        assertNull(calculateGt7FuelConsumption(null))
    }

    @Test
    fun `GT7 lapCountが0以下の場合はnullを返す`() {
        assertNull(calculateGt7FuelConsumption(sampleGt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 70f)))
    }

    @Test
    fun `GT7 消費量が0以下（燃料が減っていない）場合はnullを返す`() {
        assertNull(calculateGt7FuelConsumption(sampleGt7Telemetry(lapCount = 3, gasLevel = 70f, gasCapacity = 70f)))
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
                            surfaceTemperatureK = 0.0,
                            carcassTemperatureK = 0.0,
                            brakeTemperatureC = 0.0,
                            pressureKpa = 0.0,
                            wear = wear,
                        )
                    },
            ),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
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

    private fun sampleGt7Telemetry(
        lapCount: Int,
        gasLevel: Float,
        gasCapacity: Float,
    ) = Gt7Ps5TelemetryData(
        lapCount = lapCount,
        lapsInRace = 0,
        bestLapTimeMs = 0,
        gasLevel = gasLevel,
        gasCapacity = gasCapacity,
    )
}
