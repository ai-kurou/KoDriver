package kurou.kodriver.feature.main

import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

class AppScreenUiStateTest {
    @Test
    fun `選択中シミュレーターのIDを返す`() {
        assertEquals(
            Simulator.LmuWindows.id,
            AppScreenUiState(selectedSimulator = Simulator.LmuWindows).selectedSimulatorId,
        )
        assertEquals(
            Simulator.Gt7Ps5.id,
            AppScreenUiState(selectedSimulator = Simulator.Gt7Ps5).selectedSimulatorId,
        )
        assertEquals(
            Simulator.AceWindows.id,
            AppScreenUiState(selectedSimulator = Simulator.AceWindows).selectedSimulatorId,
        )
    }
}
