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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuWindowsTyreWearBaselineTrackerTest {
    private val tracker = LmuWindowsTyreWearBaselineTracker()

    private fun update(
        ratio: Double,
        lap: Int,
    ) = tracker.update(
        sampleLmuBaselineTelemetry(lap, mapOf(WheelIndex.FRONT_LEFT to ratio, WheelIndex.FRONT_RIGHT to ratio)),
    )

    @Test
    fun `後の輪の残溝が最小ならその値を起点にする`() {
        assertEquals(
            LmuWindowsTyreWearBaseline(0.4, 3),
            tracker.update(
                sampleLmuBaselineTelemetry(3, mapOf(WheelIndex.FRONT_LEFT to 0.8, WheelIndex.FRONT_RIGHT to 0.4)),
            ),
        )
    }

    @Test
    fun `初回の残量を起点にして消費と許容範囲の増加では維持する`() {
        baselineConsumptionSamples.forEach { sample ->
            assertEquals(
                LmuWindowsTyreWearBaseline(sample.baselineRatio, sample.baselineLap),
                update(sample.ratio, sample.lap),
            )
        }
    }

    @Test
    fun `補給または交換と周回巻き戻りで起点を取り直す`() {
        baselineRefillAndLapRollbackSamples.forEach { sample ->
            assertEquals(
                LmuWindowsTyreWearBaseline(sample.baselineRatio, sample.baselineLap),
                update(sample.ratio, sample.lap),
            )
        }
    }

    @Test
    fun `基準点より後の周回でも直前の観測より周回が戻れば起点を取り直す`() {
        baselineObservedLapRollbackSamples.forEach { sample ->
            assertEquals(
                LmuWindowsTyreWearBaseline(sample.baselineRatio, sample.baselineLap),
                update(sample.ratio, sample.lap),
            )
        }
    }

    @Test
    fun `補給または交換後の基準点から消費計算を再開する`() {
        val baseline = baselineCalculationSamples.map { sample -> update(sample.ratio, sample.lap) }.last()
        assertEquals(
            8.0,
            calculateLmuTyreWearRemainingLaps(
                sampleLmuBaselineTelemetry(7, mapOf(WheelIndex.FRONT_LEFT to 0.4)),
                baseline,
            )!!,
            0.000001,
        )
    }

    @Test
    fun `非有限値と欠測では起点を維持する`() {
        assertNull(tracker.update(null))
        update(0.5, 3)
        nonFiniteBaselineRatios.forEach { ratio ->
            assertEquals(LmuWindowsTyreWearBaseline(0.5, 3), update(ratio, 5))
        }
        assertEquals(LmuWindowsTyreWearBaseline(0.5, 3), tracker.update(null))
        baselineMissingSamples.forEach { sample ->
            val telemetry =
                sample.lap?.let { lap ->
                    val wheels = sample.ratio?.let { mapOf(WheelIndex.FRONT_LEFT to it) } ?: emptyMap()
                    sampleLmuBaselineTelemetry(lap, wheels)
                }
            assertEquals(LmuWindowsTyreWearBaseline(0.5, 3), tracker.update(telemetry))
        }
    }
}

internal data class LmuWindowsBaselineTrackerSample(
    val ratio: Double,
    val lap: Int,
    val baselineRatio: Double = ratio,
    val baselineLap: Int = lap,
)

internal val baselineConsumptionSamples =
    listOf(
        LmuWindowsBaselineTrackerSample(0.5, 3),
        LmuWindowsBaselineTrackerSample(0.4, 5, baselineRatio = 0.5, baselineLap = 3),
        LmuWindowsBaselineTrackerSample(0.403, 5, baselineRatio = 0.5, baselineLap = 3),
        LmuWindowsBaselineTrackerSample(0.406, 5),
    )

internal val baselineRefillAndLapRollbackSamples =
    listOf(
        LmuWindowsBaselineTrackerSample(0.5, 3),
        LmuWindowsBaselineTrackerSample(0.4, 5, baselineRatio = 0.5, baselineLap = 3),
        LmuWindowsBaselineTrackerSample(0.8, 5),
        LmuWindowsBaselineTrackerSample(0.7, 0),
    )

internal val baselineObservedLapRollbackSamples =
    listOf(
        LmuWindowsBaselineTrackerSample(0.5, 3),
        LmuWindowsBaselineTrackerSample(0.4, 10, baselineRatio = 0.5, baselineLap = 3),
        LmuWindowsBaselineTrackerSample(0.35, 5),
    )

internal val baselineCalculationSamples =
    listOf(
        LmuWindowsBaselineTrackerSample(0.2, 3),
        LmuWindowsBaselineTrackerSample(0.5, 5),
    )

internal data class LmuWindowsMissingBaselineSample(
    val ratio: Double?,
    val lap: Int?,
)

internal val baselineMissingSamples =
    listOf(
        LmuWindowsMissingBaselineSample(ratio = null, lap = 5),
        LmuWindowsMissingBaselineSample(ratio = 0.8, lap = null),
    )

internal val nonFiniteBaselineRatios = listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)

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
