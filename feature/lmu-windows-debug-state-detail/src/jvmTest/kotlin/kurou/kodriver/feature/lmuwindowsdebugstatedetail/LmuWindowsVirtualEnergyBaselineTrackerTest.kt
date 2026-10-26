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
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuWindowsVirtualEnergyBaselineTrackerTest {
    private val tracker = LmuWindowsVirtualEnergyBaselineTracker()

    private fun update(
        ratio: Double,
        lap: Int,
    ) = tracker.update(energy(ratio), sampleLmuTelemetry(lap))

    @Test
    fun `初回の残量を起点にして消費と許容範囲の増加では維持する`() {
        assertEquals(LmuWindowsVirtualEnergyBaseline(0.5, 3), update(0.5, 3))
        assertEquals(LmuWindowsVirtualEnergyBaseline(0.5, 3), update(0.4, 5))
        assertEquals(LmuWindowsVirtualEnergyBaseline(0.5, 3), update(0.403, 5))
        assertEquals(LmuWindowsVirtualEnergyBaseline(0.406, 5), update(0.406, 5))
    }

    @Test
    fun `補給または交換と周回巻き戻りで起点を取り直す`() {
        update(0.5, 3)
        update(0.4, 5)
        assertEquals(LmuWindowsVirtualEnergyBaseline(0.8, 5), update(0.8, 5))
        assertEquals(LmuWindowsVirtualEnergyBaseline(0.7, 0), update(0.7, 0))
    }

    @Test
    fun `基準点より後の周回でも直前の観測より周回が戻れば起点を取り直す`() {
        update(0.5, 3)
        update(0.4, 10)
        assertEquals(LmuWindowsVirtualEnergyBaseline(0.35, 5), update(0.35, 5))
    }

    @Test
    fun `補給または交換後の基準点から消費計算を再開する`() {
        update(0.2, 3)
        val baseline = update(0.5, 5)
        assertEquals(
            8.0,
            calculateLmuVirtualEnergyConsumption(energy(0.4), sampleLmuTelemetry(7), baseline)!!.preciseRemainingLaps,
            0.000001,
        )
    }

    @Test
    fun `非有限値と欠測では起点を維持する`() {
        assertNull(tracker.update(null, null))
        update(0.5, 3)
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { ratio ->
            assertEquals(LmuWindowsVirtualEnergyBaseline(0.5, 3), update(ratio, 5))
        }
        assertEquals(LmuWindowsVirtualEnergyBaseline(0.5, 3), tracker.update(null, sampleLmuTelemetry(5)))
        assertEquals(LmuWindowsVirtualEnergyBaseline(0.5, 3), tracker.update(energy(0.8), null))
    }

    private fun energy(ratio: Double) = LmuWindowsVirtualEnergyData(LmuWindowsVirtualEnergyRatio(ratio))

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
