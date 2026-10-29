package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuWindowsVirtualEnergyBaselineTrackerTest {
    private val tracker = LmuWindowsVirtualEnergyBaselineTracker()

    private fun update(
        ratio: Double,
        lap: Int,
    ) = tracker.update(energy(ratio), sampleLmuBaselineTelemetry(lap))

    @Test
    fun `初回の残量を起点にして消費と許容範囲の増加では維持する`() {
        baselineConsumptionSamples.forEach { sample ->
            assertEquals(
                LmuWindowsVirtualEnergyBaseline(sample.baselineRatio, sample.baselineLap),
                update(sample.ratio, sample.lap),
            )
        }
    }

    @Test
    fun `補給または交換と周回巻き戻りで起点を取り直す`() {
        baselineRefillAndLapRollbackSamples.forEach { sample ->
            assertEquals(
                LmuWindowsVirtualEnergyBaseline(sample.baselineRatio, sample.baselineLap),
                update(sample.ratio, sample.lap),
            )
        }
    }

    @Test
    fun `基準点より後の周回でも直前の観測より周回が戻れば起点を取り直す`() {
        baselineObservedLapRollbackSamples.forEach { sample ->
            assertEquals(
                LmuWindowsVirtualEnergyBaseline(sample.baselineRatio, sample.baselineLap),
                update(sample.ratio, sample.lap),
            )
        }
    }

    @Test
    fun `補給または交換後の基準点から消費計算を再開する`() {
        val baseline = baselineCalculationSamples.map { sample -> update(sample.ratio, sample.lap) }.last()
        assertEquals(
            8.0,
            calculateLmuVirtualEnergyConsumption(
                energy(0.4),
                sampleLmuBaselineTelemetry(7),
                baseline,
            )!!.preciseRemainingLaps,
            0.000001,
        )
    }

    @Test
    fun `非有限値と欠測では起点を維持する`() {
        assertNull(tracker.update(null, null))
        update(0.5, 3)
        nonFiniteBaselineRatios.forEach { ratio ->
            assertEquals(LmuWindowsVirtualEnergyBaseline(0.5, 3), update(ratio, 5))
        }
        baselineMissingSamples.forEach { sample ->
            assertEquals(
                LmuWindowsVirtualEnergyBaseline(0.5, 3),
                tracker.update(sample.ratio?.let { energy(it) }, sample.lap?.let { sampleLmuBaselineTelemetry(it) }),
            )
        }
    }

    private fun energy(ratio: Double) = LmuWindowsVirtualEnergyData(LmuWindowsVirtualEnergyRatio(ratio))
}
