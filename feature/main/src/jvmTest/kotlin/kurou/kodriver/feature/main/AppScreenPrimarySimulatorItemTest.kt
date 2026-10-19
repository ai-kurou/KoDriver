package kurou.kodriver.feature.main

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun `アイコンには シミュレーターを選択 のcontentDescriptionが設定される`() {
        rule.setContent {
            AppScreenPrimarySimulatorIcon(simulatorId = "lmu_windows")
        }

        rule.onNode(hasContentDescription("シミュレーターを選択")).assertExists()
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
    fun `expandedがtrueの場合シミュレーター一覧が表示される`() {
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
    fun `expandedがfalseの場合シミュレーター一覧が表示されない`() {
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
    fun `シミュレーターIDが変わると選択カードも切り替わる`() {
        var simulatorId by mutableStateOf("lmu_windows")
        rule.setContent {
            AppScreenPrimarySimulatorIndicator(
                simulatorId = simulatorId,
                expanded = true,
                onExpandedChange = {},
                onSimulatorSelected = {},
            )
        }

        val lmuCard = rule.onNode(hasText("Le Mans Ultimate（Windows版）"))
        val gt7Card = rule.onNode(hasText("Gran Turismo 7（PS5）"))
        lmuCard.assertIsSelected()
        gt7Card.assertIsNotSelected()

        simulatorId = "gt7_ps5"
        rule.mainClock.advanceTimeBy(500)

        lmuCard.assertIsNotSelected()
        gt7Card.assertIsSelected()
    }

    @Test
    fun `一覧の項目をタップすると選択コールバックが呼ばれてメニューが閉じる`() {
        var simulatorId by mutableStateOf("lmu_windows")
        var expanded by mutableStateOf(true)
        rule.mainClock.autoAdvance = false

        val feedback = mutableListOf<HapticFeedbackType>()
        val haptic =
            object : HapticFeedback {
                override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                    feedback += hapticFeedbackType
                }
            }
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppScreenPrimarySimulatorIndicator(
                    simulatorId = simulatorId,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onSimulatorSelected = { simulatorId = it },
                )
            }
        }

        val node = rule.onNode(hasText("Gran Turismo 7（PS5）"))
        node.performScrollTo()
        node.performClick()

        rule.mainClock.advanceTimeByFrame()
        rule.onNode(hasText("Gran Turismo 7（PS5）")).assertIsSelected()
        rule.onNode(hasText("Le Mans Ultimate（Windows版）")).assertIsNotSelected()
        assertEquals("gt7_ps5", simulatorId)
        assertTrue(expanded)
        rule.mainClock.advanceTimeBy(500)
        assertFalse(expanded)
        assertEquals(listOf(HapticFeedbackType.ContextClick), feedback)
    }

    @Test
    fun `選択中の項目をタップするとアニメーションを待たずにメニューが閉じる`() {
        var expanded by mutableStateOf(true)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            AppScreenPrimarySimulatorIndicator(
                simulatorId = "lmu_windows",
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onSimulatorSelected = {},
            )
        }

        rule.onNode(hasText("Le Mans Ultimate（Windows版）")).performClick()

        assertFalse(expanded)
    }
}
