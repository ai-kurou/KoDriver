package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData

private const val REFILL_DETECTION_TOLERANCE_LITERS = 0.5

data class LmuWindowsFuelBaseline(
    val liters: Double,
    val lap: Int,
)

/**
 * 燃料の消費量を求める起点（観測開始時・給油後・周回数の巻き戻り後）を保持する。
 * タンク容量ではなく観測した燃料量との差分から消費量を求めるために使う。
 */
internal class LmuWindowsFuelBaselineTracker {
    private var baseline: LmuWindowsFuelBaseline? = null
    private var lowestLitersSinceBaseline = 0.0

    fun update(telemetry: LmuWindowsTelemetryData?): LmuWindowsFuelBaseline? {
        val fuel = telemetry?.fuel ?: return baseline
        val liters = fuel.currentLiters.value
        val capacityLiters = fuel.capacityLiters.value
        if (!liters.isFinite() || !capacityLiters.isFinite() || capacityLiters <= 0.0) return baseline
        val lap = telemetry.timing.currentLap
        val current = baseline
        // 給油が複数フレームに分かれて微小に増える場合も検出できるよう、起点以降の最小値からの増加量で判定する。
        val refilled = liters - lowestLitersSinceBaseline > REFILL_DETECTION_TOLERANCE_LITERS
        if (current == null || lap < current.lap || refilled) {
            baseline = LmuWindowsFuelBaseline(liters, lap)
            lowestLitersSinceBaseline = liters
        } else {
            lowestLitersSinceBaseline = minOf(lowestLitersSinceBaseline, liters)
        }
        return baseline
    }
}
