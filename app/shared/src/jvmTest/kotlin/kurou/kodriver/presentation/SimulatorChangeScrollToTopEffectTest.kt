package kurou.kodriver.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SimulatorChangeScrollToTopEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `初回コンポジションでは先頭スクロールを要求しない`() {
        var requestScrollToTopCount = 0

        composeRule.setContent {
            SimulatorChangeScrollToTopEffect(
                selectedSimulatorId = "lmu_windows",
                requestScrollToTop = { requestScrollToTopCount++ },
            )
        }

        composeRule.waitForIdle()

        assertEquals(0, requestScrollToTopCount)
    }

    @Test
    fun `シミュレータIDが変更されると先頭スクロールを要求する`() {
        var selectedSimulatorId by mutableStateOf("lmu_windows")
        var requestScrollToTopCount = 0

        composeRule.setContent {
            SimulatorChangeScrollToTopEffect(
                selectedSimulatorId = selectedSimulatorId,
                requestScrollToTop = { requestScrollToTopCount++ },
            )
        }

        composeRule.waitForIdle()
        selectedSimulatorId = "ace_windows"
        composeRule.waitForIdle()

        assertEquals(1, requestScrollToTopCount)
    }

    @Test
    fun `同一のシミュレータIDが再度渡されても先頭スクロールを要求しない`() {
        var selectedSimulatorId by mutableStateOf("lmu_windows")
        var requestScrollToTopCount = 0

        composeRule.setContent {
            SimulatorChangeScrollToTopEffect(
                selectedSimulatorId = selectedSimulatorId,
                requestScrollToTop = { requestScrollToTopCount++ },
            )
        }

        composeRule.waitForIdle()
        selectedSimulatorId = "lmu_windows"
        composeRule.waitForIdle()

        assertEquals(0, requestScrollToTopCount)
    }
}
