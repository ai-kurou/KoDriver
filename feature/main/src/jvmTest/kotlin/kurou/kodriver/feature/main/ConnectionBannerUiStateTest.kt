@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.main

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionBannerUiStateTest {
    @Test
    fun `isGt7Ps5_Gt7Ps5のときtrue`() {
        val state = ConnectionBannerVmUiState(selectedSimulator = Simulator.Gt7Ps5)
        assertTrue(state.isGt7Ps5)
    }

    @Test
    fun `isGt7Ps5_Gt7Ps5以外のときfalse`() {
        for (simulator in listOf(
            Simulator.LmuWindows,
            Simulator.AceWindows,
        )) {
            val state = ConnectionBannerVmUiState(selectedSimulator = simulator)
            assertFalse(state.isGt7Ps5, "simulator=$simulator のとき isGt7Ps5 は false であるべき")
        }
    }

    @Test
    fun `isAceWindows_AceWindowsのときtrue`() {
        val state = ConnectionBannerVmUiState(selectedSimulator = Simulator.AceWindows)
        assertTrue(state.isAceWindows)
    }

    @Test
    fun `isAceWindows_AceWindows以外のときfalse`() {
        for (simulator in listOf(
            Simulator.LmuWindows,
            Simulator.Gt7Ps5,
        )) {
            val state = ConnectionBannerVmUiState(selectedSimulator = simulator)
            assertFalse(state.isAceWindows, "simulator=$simulator のとき isAceWindows は false であるべき")
        }
    }
}
