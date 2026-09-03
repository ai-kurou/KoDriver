package kurou.kodriver.feature.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AppScreenPrimarySimulatorItemTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `simulatorIdが指定されている場合アイコンが例外なく描画される`() {
        rule.setContent {
            AppScreenPrimarySimulatorIcon(simulatorId = "lmu_windows")
        }
    }

    @Test
    fun `アイコンには シミュレータを選択 のcontentDescriptionが設定される`() {
        rule.setContent {
            AppScreenPrimarySimulatorIcon(simulatorId = "lmu_windows")
        }

        rule.onNode(hasContentDescription("シミュレータを選択")).assertExists()
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

    @Test
    fun `expandedがtrueの場合シミュレータ一覧が表示される`() {
        rule.setContent {
            AppScreenPrimarySimulatorIndicator(
                simulatorId = "lmu_windows",
                expanded = true,
                onExpandedChange = {},
                onSimulatorSelected = {},
            )
        }

        rule.onNode(hasText("Le Mans Ultimate（Windows版）")).assertExists()
        rule.onNode(hasText("Gran Turismo 7（PS5）")).assertExists()
        rule.onNode(hasText("Assetto Corsa EVO（Windows版）")).assertExists()
    }

    @Test
    fun `expandedがfalseの場合シミュレータ一覧が表示されない`() {
        rule.setContent {
            AppScreenPrimarySimulatorIndicator(
                simulatorId = "lmu_windows",
                expanded = false,
                onExpandedChange = {},
                onSimulatorSelected = {},
            )
        }

        rule.onNode(hasText("Le Mans Ultimate（Windows版）")).assertDoesNotExist()
    }

    @Test
    fun `一覧の項目をタップすると選択コールバックが呼ばれてメニューが閉じる`() {
        var selectedId: String? = null
        var expanded by mutableStateOf(true)

        rule.setContent {
            AppScreenPrimarySimulatorIndicator(
                simulatorId = "lmu_windows",
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onSimulatorSelected = { selectedId = it },
            )
        }

        val node = rule.onNode(hasText("Gran Turismo 7（PS5）"))
        node.performScrollTo()
        node.performClick()

        assertEquals("gt7_ps5", selectedId)
        assertFalse(expanded)
    }
}
