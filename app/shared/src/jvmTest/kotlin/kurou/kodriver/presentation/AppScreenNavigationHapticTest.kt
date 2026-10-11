package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenNavigationHapticTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `NavigationBarのタブ選択と再タップは振動設定trueに従う`() {
        val haptic = FakeHapticFeedback()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    hapticFeedbackEnabled = true,
                )
            }
        }

        listOf("ルール", "ログ", "その他").forEach { label ->
            repeat(2) {
                rule.onNode(hasText(label)).performClick()
                rule.waitForIdle()
            }
        }

        assertEquals(List(6) { HapticFeedbackType.ContextClick }, haptic.performedTypes)
    }

    @Test
    fun `NavigationBarのシミュレータメニューと選択は振動設定trueに従う`() {
        val haptic = FakeHapticFeedback()
        var selectedId: String? = null
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    hapticFeedbackEnabled = true,
                    selectedSimulatorId = "lmu_windows",
                    onSimulatorSelected = { selectedId = it },
                )
            }
        }

        rule.onNodeWithTag("primarySimulatorNavItem").performClick()
        rule.waitForIdle()
        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
        rule.onNode(hasText("Gran Turismo 7（PS5）")).performClick()
        rule.waitForIdle()

        assertEquals("gt7_ps5", selectedId)
        rule.onNodeWithTag("simulatorSelectionPopup").assertDoesNotExist()
        assertEquals(List(2) { HapticFeedbackType.ContextClick }, haptic.performedTypes)
    }

    @Test
    fun `NavigationBarのタブ選択と再タップは振動設定falseに従う`() {
        val haptic = FakeHapticFeedback()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    hapticFeedbackEnabled = false,
                )
            }
        }

        listOf("ルール", "ログ", "その他").forEach { label ->
            repeat(2) {
                rule.onNode(hasText(label)).performClick()
                rule.waitForIdle()
            }
        }

        assertEquals(emptyList(), haptic.performedTypes)
    }

    @Test
    fun `NavigationBarのシミュレータメニューと選択は振動設定falseに従う`() {
        val haptic = FakeHapticFeedback()
        var selectedId: String? = null
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    hapticFeedbackEnabled = false,
                    selectedSimulatorId = "lmu_windows",
                    onSimulatorSelected = { selectedId = it },
                )
            }
        }

        rule.onNodeWithTag("primarySimulatorNavItem").performClick()
        rule.waitForIdle()
        assertEquals(emptyList(), haptic.performedTypes)
        rule.onNode(hasText("Gran Turismo 7（PS5）")).performClick()
        rule.waitForIdle()

        assertEquals("gt7_ps5", selectedId)
        rule.onNodeWithTag("simulatorSelectionPopup").assertDoesNotExist()
        assertEquals(emptyList(), haptic.performedTypes)
    }

    @Test
    fun `NavigationRailのタブ選択と再タップは振動設定trueに従う`() {
        val haptic = FakeHapticFeedback()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationRail,
                    hapticFeedbackEnabled = true,
                )
            }
        }

        listOf("ルール", "ログ", "その他").forEach { label ->
            repeat(2) {
                rule.onNode(hasText(label)).performClick()
                rule.waitForIdle()
            }
        }

        assertEquals(List(6) { HapticFeedbackType.ContextClick }, haptic.performedTypes)
    }

    @Test
    fun `NavigationRailのシミュレータメニューと選択は振動設定trueに従う`() {
        val haptic = FakeHapticFeedback()
        var selectedId: String? = null
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationRail,
                    hapticFeedbackEnabled = true,
                    selectedSimulatorId = "lmu_windows",
                    onSimulatorSelected = { selectedId = it },
                )
            }
        }

        rule.onNodeWithTag("primarySimulatorNavItem").performClick()
        rule.waitForIdle()
        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
        rule.onNode(hasText("Gran Turismo 7（PS5）")).performClick()
        rule.waitForIdle()

        assertEquals("gt7_ps5", selectedId)
        rule.onNodeWithTag("simulatorSelectionPopup").assertDoesNotExist()
        assertEquals(List(2) { HapticFeedbackType.ContextClick }, haptic.performedTypes)
    }

    @Test
    fun `NavigationRailのタブ選択と再タップは振動設定falseに従う`() {
        val haptic = FakeHapticFeedback()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationRail,
                    hapticFeedbackEnabled = false,
                )
            }
        }

        listOf("ルール", "ログ", "その他").forEach { label ->
            repeat(2) {
                rule.onNode(hasText(label)).performClick()
                rule.waitForIdle()
            }
        }

        assertEquals(emptyList(), haptic.performedTypes)
    }

    @Test
    fun `NavigationRailのシミュレータメニューと選択は振動設定falseに従う`() {
        val haptic = FakeHapticFeedback()
        var selectedId: String? = null
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationRail,
                    hapticFeedbackEnabled = false,
                    selectedSimulatorId = "lmu_windows",
                    onSimulatorSelected = { selectedId = it },
                )
            }
        }

        rule.onNodeWithTag("primarySimulatorNavItem").performClick()
        rule.waitForIdle()
        assertEquals(emptyList(), haptic.performedTypes)
        rule.onNode(hasText("Gran Turismo 7（PS5）")).performClick()
        rule.waitForIdle()

        assertEquals("gt7_ps5", selectedId)
        rule.onNodeWithTag("simulatorSelectionPopup").assertDoesNotExist()
        assertEquals(emptyList(), haptic.performedTypes)
    }
}
