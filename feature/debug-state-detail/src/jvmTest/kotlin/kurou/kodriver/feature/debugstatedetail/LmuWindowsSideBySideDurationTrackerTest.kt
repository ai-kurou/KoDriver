package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsSideBySideDurationTrackerTest {
    private val tracker = LmuWindowsSideBySideDurationTracker()

    private fun data(
        left: Set<Int> = emptySet(),
        right: Set<Int> = emptySet(),
    ) = LmuWindowsVehicleApproachData(left, right, LateralDistanceMeters(1.0), LateralDistanceMeters(2.0))

    @Test
    fun `車両がいなければ左右ともnullになる`() {
        assertEquals(LmuWindowsSideBySideDurations(null, null), tracker.update(data(), 100))
    }

    @Test
    fun `新規車両はゼロから始まり継続車両は左右独立に計測する`() {
        assertEquals(LmuWindowsSideBySideDurations(0, null), tracker.update(data(left = setOf(1)), 100))
        assertEquals(LmuWindowsSideBySideDurations(100, 0), tracker.update(data(setOf(1), setOf(1)), 200))
        assertEquals(LmuWindowsSideBySideDurations(350, 250), tracker.update(data(setOf(1), setOf(1)), 450))
    }

    @Test
    fun `複数車両では最長時間を返し消えた車両の開始時刻を破棄する`() {
        tracker.update(data(setOf(1), setOf(3)), 100)
        assertEquals(LmuWindowsSideBySideDurations(100, 100), tracker.update(data(setOf(1, 2), setOf(3, 4)), 200))
        assertEquals(LmuWindowsSideBySideDurations(50, 50), tracker.update(data(setOf(2), setOf(4)), 250))
        assertEquals(LmuWindowsSideBySideDurations(null, null), tracker.update(data(), 300))
        assertEquals(LmuWindowsSideBySideDurations(0, 0), tracker.update(data(setOf(1), setOf(3)), 400))
    }

    @Test
    fun `車両IDの入れ替えと左右の移動で計測をリセットする`() {
        tracker.update(data(setOf(1), setOf(2)), 100)
        assertEquals(LmuWindowsSideBySideDurations(0, 0), tracker.update(data(setOf(2), setOf(1)), 200))
        assertEquals(LmuWindowsSideBySideDurations(0, 100), tracker.update(data(setOf(3), setOf(1)), 300))
    }
}
