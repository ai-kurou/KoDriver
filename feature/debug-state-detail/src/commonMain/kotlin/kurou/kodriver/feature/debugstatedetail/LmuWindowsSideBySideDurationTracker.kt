package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData

/** デバッグ画面で観測した、左右それぞれの最長並走時間。 */
data class LmuWindowsSideBySideDurations(
    val leftMillis: Long?,
    val rightMillis: Long?,
)

internal class LmuWindowsSideBySideDurationTracker {
    private val leftStartTimes = mutableMapOf<Int, Long>()
    private val rightStartTimes = mutableMapOf<Int, Long>()

    fun update(
        data: LmuWindowsVehicleApproachData,
        nowMs: Long,
    ): LmuWindowsSideBySideDurations =
        LmuWindowsSideBySideDurations(
            leftMillis = updateSide(leftStartTimes, data.sideBySideLeftVehicleIds, nowMs),
            rightMillis = updateSide(rightStartTimes, data.sideBySideRightVehicleIds, nowMs),
        )

    private fun updateSide(
        startTimes: MutableMap<Int, Long>,
        vehicleIds: Set<Int>,
        nowMs: Long,
    ): Long? {
        startTimes.keys.retainAll(vehicleIds)
        vehicleIds.forEach { startTimes.getOrPut(it) { nowMs } }
        return startTimes.values.minOrNull()?.let { nowMs - it }
    }
}
