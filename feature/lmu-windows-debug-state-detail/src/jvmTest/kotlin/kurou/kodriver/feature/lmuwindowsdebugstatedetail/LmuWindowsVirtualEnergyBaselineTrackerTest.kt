package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio

class LmuWindowsVirtualEnergyBaselineTrackerTest :
    LmuWindowsBaselineTrackerContractTest<LmuWindowsVirtualEnergyBaseline>() {
    private val tracker = LmuWindowsVirtualEnergyBaselineTracker()

    override fun update(
        ratio: Double?,
        lap: Int?,
    ) = tracker.update(ratio?.let { energy(it) }, lap?.let { sampleLmuBaselineTelemetry(it) })

    override fun baseline(
        ratio: Double,
        lap: Int,
    ) = LmuWindowsVirtualEnergyBaseline(ratio, lap)

    override fun remainingLaps(baseline: LmuWindowsVirtualEnergyBaseline?) =
        calculateLmuVirtualEnergyConsumption(
            energy(0.4),
            sampleLmuBaselineTelemetry(7),
            baseline,
        )?.preciseRemainingLaps

    private fun energy(ratio: Double) = LmuWindowsVirtualEnergyData(LmuWindowsVirtualEnergyRatio(ratio))
}
