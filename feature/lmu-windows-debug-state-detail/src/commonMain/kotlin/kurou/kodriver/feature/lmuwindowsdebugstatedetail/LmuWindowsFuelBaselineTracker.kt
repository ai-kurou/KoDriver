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
    private var lastLiters = 0.0

    fun update(telemetry: LmuWindowsTelemetryData?): LmuWindowsFuelBaseline? {
        val liters = telemetry?.fuel?.currentLiters?.value
        if (telemetry == null || liters == null || !liters.isFinite()) return baseline
        val lap = telemetry.timing.currentLap
        val current = baseline
        if (current == null || lap < current.lap || liters > lastLiters + REFILL_DETECTION_TOLERANCE_LITERS) {
            baseline = LmuWindowsFuelBaseline(liters, lap)
        }
        lastLiters = liters
        return baseline
    }
}
