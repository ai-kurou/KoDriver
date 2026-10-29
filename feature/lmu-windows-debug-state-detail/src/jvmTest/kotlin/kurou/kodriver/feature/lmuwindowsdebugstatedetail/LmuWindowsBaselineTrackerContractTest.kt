package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// 継承先ごとに5ケースを実行し、入力形式と計算APIの違いは各trackerのテストで扱う。
abstract class LmuWindowsBaselineTrackerContractTest<B : Any> {
    protected abstract fun update(
        ratio: Double?,
        lap: Int?,
    ): B?

    protected abstract fun baseline(
        ratio: Double,
        lap: Int,
    ): B

    protected abstract fun remainingLaps(baseline: B?): Double?

    @Test
    fun `初回の残量を起点にして消費と許容範囲の増加では維持する`() {
        baselineConsumptionSamples.forEach { sample ->
            assertEquals(
                baseline(sample.baselineRatio, sample.baselineLap),
                update(sample.ratio, sample.lap),
            )
        }
    }

    @Test
    fun `補給または交換と周回巻き戻りで起点を取り直す`() {
        baselineRefillAndLapRollbackSamples.forEach { sample ->
            assertEquals(
                baseline(sample.baselineRatio, sample.baselineLap),
                update(sample.ratio, sample.lap),
            )
        }
    }

    @Test
    fun `基準点より後の周回でも直前の観測より周回が戻れば起点を取り直す`() {
        baselineObservedLapRollbackSamples.forEach { sample ->
            assertEquals(
                baseline(sample.baselineRatio, sample.baselineLap),
                update(sample.ratio, sample.lap),
            )
        }
    }

    @Test
    fun `補給または交換後の基準点から消費計算を再開する`() {
        val baseline = baselineCalculationSamples.map { sample -> update(sample.ratio, sample.lap) }.last()
        assertEquals(8.0, remainingLaps(baseline)!!, 0.000001)
    }

    @Test
    fun `非有限値と欠測では起点を維持する`() {
        assertNull(update(null, null))
        update(0.5, 3)
        nonFiniteBaselineRatios.forEach { ratio ->
            assertEquals(baseline(0.5, 3), update(ratio, 5))
        }
        assertEquals(baseline(0.5, 3), update(null, null))
        baselineMissingSamples.forEach { sample ->
            assertEquals(baseline(0.5, 3), update(sample.ratio, sample.lap))
        }
    }
}

private data class LmuWindowsBaselineTrackerSample(
    val ratio: Double,
    val lap: Int,
    val baselineRatio: Double = ratio,
    val baselineLap: Int = lap,
)

private val baselineConsumptionSamples =
    listOf(
        LmuWindowsBaselineTrackerSample(0.5, 3),
        LmuWindowsBaselineTrackerSample(0.4, 5, baselineRatio = 0.5, baselineLap = 3),
        LmuWindowsBaselineTrackerSample(0.403, 5, baselineRatio = 0.5, baselineLap = 3),
        LmuWindowsBaselineTrackerSample(0.406, 5),
    )

private val baselineRefillAndLapRollbackSamples =
    listOf(
        LmuWindowsBaselineTrackerSample(0.5, 3),
        LmuWindowsBaselineTrackerSample(0.4, 5, baselineRatio = 0.5, baselineLap = 3),
        LmuWindowsBaselineTrackerSample(0.8, 5),
        LmuWindowsBaselineTrackerSample(0.7, 0),
    )

private val baselineObservedLapRollbackSamples =
    listOf(
        LmuWindowsBaselineTrackerSample(0.5, 3),
        LmuWindowsBaselineTrackerSample(0.4, 10, baselineRatio = 0.5, baselineLap = 3),
        LmuWindowsBaselineTrackerSample(0.35, 5),
    )

private val baselineCalculationSamples =
    listOf(
        LmuWindowsBaselineTrackerSample(0.2, 3),
        LmuWindowsBaselineTrackerSample(0.5, 5),
    )

private data class LmuWindowsMissingBaselineSample(
    val ratio: Double?,
    val lap: Int?,
)

private val baselineMissingSamples =
    listOf(
        LmuWindowsMissingBaselineSample(ratio = null, lap = 5),
        LmuWindowsMissingBaselineSample(ratio = 0.8, lap = null),
    )

private val nonFiniteBaselineRatios = listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
