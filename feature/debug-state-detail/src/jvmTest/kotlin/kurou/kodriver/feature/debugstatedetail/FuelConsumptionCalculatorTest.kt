package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FuelConsumptionCalculatorTest {

    @Test
    fun `LMU 5周消費後の残量50パーセントなら1周あたり10パーセント消費で残り5周`() {
        val result = calculateLmuVirtualEnergyConsumption(
            virtualEnergy = LmuWindowsVirtualEnergyData(remainingRatio = 0.5),
            telemetry = sampleLmuTelemetry(currentLap = 5),
        )

        assertEquals(10.0, result?.consumptionPerLap)
        assertEquals(5, result?.remainingLaps)
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
        val result = calculateGt7FuelConsumption(
            sampleGt7Telemetry(lapCount = 3, gasLevel = 40f, gasCapacity = 70f),
        )

        assertEquals(10.0, result?.consumptionPerLap)
        assertEquals(4, result?.remainingLaps)
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

    private fun sampleLmuTelemetry(currentLap: Int) = LmuWindowsTelemetryData(
        timestampMs = 0L,
        engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
        inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
        tyres = LmuWindowsTyreData(wheels = emptyMap()),
        fuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0),
        timing = LmuWindowsTimingData(
            currentLapTimeMs = 0L,
            lastLapTimeMs = 0L,
            bestLapTimeMs = 0L,
            sector1Ms = 0L,
            sector1And2Ms = 0L,
            currentLap = currentLap,
            maxLaps = 0,
        ),
        vehicle = LmuWindowsVehicleData(
            localVelocityX = 0.0, localVelocityY = 0.0, localVelocityZ = 0.0,
            positionX = 0.0, positionY = 0.0, positionZ = 0.0,
        ),
    )

    private fun sampleGt7Telemetry(lapCount: Int, gasLevel: Float, gasCapacity: Float) = Gt7Ps5TelemetryData(
        lapCount = lapCount,
        lapsInRace = 0,
        bestLapTimeMs = 0,
        gasLevel = gasLevel,
        gasCapacity = gasCapacity,
    )
}
