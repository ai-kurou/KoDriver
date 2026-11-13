package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_RATIO_INCREASE_DETECTION_TOLERANCE_RATIO
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData

data class LmuWindowsTyreWearBaseline(
    val ratio: Double,
    val lap: Int,
)

/**
 * 残量の消費量を求める起点（観測開始時・補給／交換後・周回数の巻き戻り後）を保持する。
 */
internal class LmuWindowsTyreWearBaselineTracker {
    private var baseline: LmuWindowsTyreWearBaseline? = null
    private var lowestRatioSinceBaseline = 0.0
    private var lastLap = 0

    fun update(telemetry: LmuWindowsTelemetryData?): LmuWindowsTyreWearBaseline? {
        val ratio =
            telemetry
                ?.tyres
                ?.wheels
                ?.values
                ?.minOfOrNull { it.wear }
                ?.value ?: return baseline
        if (!ratio.isFinite()) return baseline
        val lap = telemetry.timing.currentLap
        val current = baseline
        // 補給・交換が複数フレームに分かれて微小に増える場合も検出できるよう、起点以降の最小値からの増加量で判定する。
        val refilled = ratio - lowestRatioSinceBaseline > LMU_WINDOWS_REMAINING_RATIO_INCREASE_DETECTION_TOLERANCE_RATIO
        if (current == null || lap < lastLap || refilled) {
            baseline = LmuWindowsTyreWearBaseline(ratio, lap)
            lowestRatioSinceBaseline = ratio
        } else {
            lowestRatioSinceBaseline = minOf(lowestRatioSinceBaseline, ratio)
        }
        lastLap = lap
        return baseline
    }
}
