package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsInputsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuWindowsFuelBaselineTrackerTest {
    private val tracker = LmuWindowsFuelBaselineTracker()

    private fun telemetry(
        liters: Double,
        lap: Int,
    ): LmuWindowsTelemetryData =
        sampleLmuTelemetry(lap).copy(
            fuel = LmuWindowsFuelData(LmuWindowsFuelUnit(liters), LmuWindowsFuelUnit(100.0)),
        )

    @Test
    fun `テレメトリ未取得の間はnullを返す`() {
        assertNull(tracker.update(null))
    }

    @Test
    fun `最初に観測した燃料量と周回数を起点にし消費中は維持する`() {
        assertEquals(LmuWindowsFuelBaseline(20.0, 2), tracker.update(telemetry(20.0, 2)))
        assertEquals(LmuWindowsFuelBaseline(20.0, 2), tracker.update(telemetry(15.0, 3)))
        assertEquals(LmuWindowsFuelBaseline(20.0, 2), tracker.update(telemetry(15.2, 3)))
        assertEquals(LmuWindowsFuelBaseline(20.0, 2), tracker.update(null))
    }

    @Test
    fun `給油で燃料量が増えたら起点を取り直す`() {
        tracker.update(telemetry(10.0, 4))
        assertEquals(LmuWindowsFuelBaseline(60.0, 5), tracker.update(telemetry(60.0, 5)))
    }

    @Test
    fun `周回数が巻き戻ったら起点を取り直す`() {
        tracker.update(telemetry(10.0, 4))
        assertEquals(LmuWindowsFuelBaseline(9.0, 0), tracker.update(telemetry(9.0, 0)))
    }

    @Test
    fun `燃料量が有限でない場合は起点を更新しない`() {
        tracker.update(telemetry(10.0, 4))
        assertEquals(LmuWindowsFuelBaseline(10.0, 4), tracker.update(telemetry(Double.NaN, 5)))
    }

    private fun sampleLmuTelemetry(currentLap: Int) =
        LmuWindowsTelemetryData(
            timestampMs = 0L,
            engine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
            inputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
            tyres = LmuWindowsTyreData(wheels = emptyMap()),
            fuel =
                LmuWindowsFuelData(
                    currentLiters = LmuWindowsFuelUnit(0.0),
                    capacityLiters = LmuWindowsFuelUnit(0.0),
                ),
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
