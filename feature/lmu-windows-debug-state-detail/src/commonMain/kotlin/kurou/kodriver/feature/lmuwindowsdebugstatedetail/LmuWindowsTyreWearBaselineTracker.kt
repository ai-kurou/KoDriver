package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData

private const val INCREASE_DETECTION_TOLERANCE_RATIO = 0.005 // 残量の微小な揺れを補給・交換と誤認しないよう0.5%を許容する。

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
        val refilled = ratio - lowestRatioSinceBaseline > INCREASE_DETECTION_TOLERANCE_RATIO
        if (current == null || lap < current.lap || refilled) {
            baseline = LmuWindowsTyreWearBaseline(ratio, lap)
            lowestRatioSinceBaseline = ratio
        } else {
            lowestRatioSinceBaseline = minOf(lowestRatioSinceBaseline, ratio)
        }
        return baseline
    }
}
