package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreWearBaselineTrackerTest : LmuWindowsBaselineTrackerContractTest<LmuWindowsTyreWearBaseline>() {
    private val tracker = LmuWindowsTyreWearBaselineTracker()

    override fun update(
        ratio: Double?,
        lap: Int?,
    ) = tracker.update(
        lap?.let {
            val wheels = ratio?.let { mapOf(WheelIndex.FRONT_LEFT to it, WheelIndex.FRONT_RIGHT to it) } ?: emptyMap()
            sampleLmuBaselineTelemetry(it, wheels)
        },
    )

    override fun baseline(
        ratio: Double,
        lap: Int,
    ) = LmuWindowsTyreWearBaseline(ratio, lap)

    override fun remainingLaps(baseline: LmuWindowsTyreWearBaseline?) =
        calculateLmuTyreWearRemainingLaps(
            sampleLmuBaselineTelemetry(7, mapOf(WheelIndex.FRONT_LEFT to 0.4)),
            baseline,
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
}
