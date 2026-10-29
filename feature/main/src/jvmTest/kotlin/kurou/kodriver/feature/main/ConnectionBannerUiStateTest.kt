package kurou.kodriver.feature.main

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionBannerUiStateTest {
    @Test
    fun `全シミュレーターで各判定プロパティの期待値を返す`() {
        for (simulator in listOf(Simulator.LmuWindows, Simulator.Gt7Ps5, Simulator.AceWindows)) {
            val state = ConnectionBannerVmUiState(selectedSimulator = simulator)

            if (simulator == Simulator.Gt7Ps5) {
                assertTrue(state.isGt7Ps5)
            } else {
                assertFalse(state.isGt7Ps5, "simulator=$simulator のとき isGt7Ps5 は false であるべき")
            }

            if (simulator == Simulator.AceWindows) {
                assertTrue(state.isAceWindows)
            } else {
                assertFalse(state.isAceWindows, "simulator=$simulator のとき isAceWindows は false であるべき")
            }
        }
    }
}
