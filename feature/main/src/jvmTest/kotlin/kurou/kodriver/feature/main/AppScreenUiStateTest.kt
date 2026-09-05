@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.main

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

class AppScreenUiStateTest {
    @Test
    fun `selectedSimulatorId_LmuWindowsのときlmu_windows`() {
        val state = AppScreenUiState(selectedSimulator = Simulator.LmuWindows)
        assertEquals(Simulator.LmuWindows.id, state.selectedSimulatorId)
    }

    @Test
    fun `selectedSimulatorId_Gt7Ps5のときgt7_ps5`() {
        val state = AppScreenUiState(selectedSimulator = Simulator.Gt7Ps5)
        assertEquals(Simulator.Gt7Ps5.id, state.selectedSimulatorId)
    }

    @Test
    fun `selectedSimulatorId_AceWindowsのときace_windows`() {
        val state = AppScreenUiState(selectedSimulator = Simulator.AceWindows)
        assertEquals(Simulator.AceWindows.id, state.selectedSimulatorId)
    }
}
