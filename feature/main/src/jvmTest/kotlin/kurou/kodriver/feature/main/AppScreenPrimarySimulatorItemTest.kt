package kurou.kodriver.feature.main

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenPrimarySimulatorItemTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `simulatorIdがnullの場合フォールバックアイコンが例外なく描画される`() {
        rule.setContent {
            AppScreenPrimarySimulatorIcon(simulatorId = null)
        }
    }

    @Test
    fun `simulatorIdが指定されている場合アイコンが例外なく描画される`() {
        rule.setContent {
            AppScreenPrimarySimulatorIcon(simulatorId = "lmu_windows")
        }
    }

    @Test
    fun `simulatorIdがnullの場合ラベルは未選択になる`() {
        var label = ""
        rule.setContent {
            label = appScreenPrimarySimulatorLabel(simulatorId = null)
        }

        assertEquals("未選択", label)
    }

    @Test
    fun `lmu_windowsの場合ラベルはLMUになる`() {
        var label = ""
        rule.setContent {
            label = appScreenPrimarySimulatorLabel(simulatorId = "lmu_windows")
        }

        assertEquals("LMU", label)
    }

    @Test
    fun `gt7_ps5の場合ラベルはGT7になる`() {
        var label = ""
        rule.setContent {
            label = appScreenPrimarySimulatorLabel(simulatorId = "gt7_ps5")
        }

        assertEquals("GT7", label)
    }

    @Test
    fun `ace_windowsの場合ラベルはACEになる`() {
        var label = ""
        rule.setContent {
            label = appScreenPrimarySimulatorLabel(simulatorId = "ace_windows")
        }

        assertEquals("ACE", label)
    }
}
