package kurou.kodriver.presentation

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `両バナー表示時だけ区切り線を表示する`() {
        val connectionVisible = mutableStateOf(true)
        val reason = mutableStateOf<TtsUnavailableGuidance?>(TtsUnavailableGuidance.EngineMissing)
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                bannerUiState = ConnectionBannerUiState(isVisible = connectionVisible.value),
                ttsUnavailableGuidance = reason.value,
            )
        }
        rule.onNodeWithTag("connectionTtsBannerDivider").assertIsDisplayed()
        rule.runOnIdle { connectionVisible.value = false }
        rule.onNodeWithTag("connectionTtsBannerDivider").assertDoesNotExist()
        rule.runOnIdle { reason.value = null }
        rule.onNodeWithTag("connectionTtsBannerDivider").assertDoesNotExist()
        rule.runOnIdle { connectionVisible.value = true }
        rule.onNodeWithTag("connectionTtsBannerDivider").assertDoesNotExist()
        rule.runOnIdle { reason.value = TtsUnavailableGuidance.EngineMissing }
        rule.onNodeWithTag("connectionTtsBannerDivider").assertIsDisplayed()
    }

    @Test
    fun `接続状況とTTS警告を同時表示し警告をタップしてもタブは変わらない`() {
        var actionCount = 0
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                bannerUiState =
                    ConnectionBannerUiState(
                        status = ConnectionBannerStatus.CONNECTED,
                        message = "シミュレーターに接続中",
                    ),
                ttsUnavailableGuidance = TtsUnavailableGuidance.EngineMissing,
                onTtsAction = { actionCount++ },
                readoutContent = { Text("読み上げ画面") },
                otherContent = { Text("その他画面") },
            )
        }
        rule.onNodeWithText("シミュレーターに接続中").assertIsDisplayed()
        rule.onNodeWithText("このまま使う").assertDoesNotExist()
        rule.onNodeWithText("音声読み上げを利用できません").performClick()
        rule.onNodeWithText("インストール").performClick()
        assertEquals(1, actionCount)
        rule.onNodeWithText("このまま使う").performClick()
        rule.onNodeWithText("読み上げ画面").assertIsDisplayed()
        rule.onNodeWithText("その他画面").assertDoesNotExist()
        rule.onNodeWithText("シミュレーターに接続中").assertIsDisplayed()
    }

    @Test
    fun `expanded幅ではNavigationRailを使用する`() {
        val layoutType =
            WindowSizeClass(
                minWidthDp = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
                minHeightDp = 0,
            ).resolveNavigationSuiteType()

        assertEquals(NavigationSuiteType.NavigationRail, layoutType)
    }

    @Test
    fun `medium幅ではNavigationRailを使用する`() {
        val layoutType =
            WindowSizeClass(
                minWidthDp = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
                minHeightDp = 0,
            ).resolveNavigationSuiteType()

        assertEquals(NavigationSuiteType.NavigationRail, layoutType)
    }

    @Test
    fun `compact幅ではNavigationBarを使用する`() {
        val layoutType =
            WindowSizeClass(
                minWidthDp = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND - 1,
                minHeightDp = 0,
            ).resolveNavigationSuiteType()

        assertEquals(NavigationSuiteType.NavigationBar, layoutType)
    }

    @Test
    fun `ルールタブを再タップするとonReadoutTabReselectedが呼ばれる`() {
        var readoutReselectedCount = 0
        var logReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onLogTabReselected = { logReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNode(hasText("ルール")).performClick()
        rule.waitForIdle()

        assertEquals(1, readoutReselectedCount)
        assertEquals(0, logReselectedCount)
        assertEquals(0, otherReselectedCount)
    }

    @Test
    fun `ログタブを再タップするとonLogTabReselectedが呼ばれる`() {
        var readoutReselectedCount = 0
        var logReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onLogTabReselected = { logReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNode(hasText("ログ")).performClick()
        rule.waitForIdle()
        assertEquals(0, logReselectedCount)

        rule.onNode(hasText("ログ")).performClick()
        rule.waitForIdle()

        assertEquals(0, readoutReselectedCount)
        assertEquals(1, logReselectedCount)
        assertEquals(0, otherReselectedCount)
    }

    @Test
    fun `その他タブを再タップするとonOtherTabReselectedが呼ばれる`() {
        var readoutReselectedCount = 0
        var logReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onLogTabReselected = { logReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()
        assertEquals(0, otherReselectedCount)

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()

        assertEquals(0, readoutReselectedCount)
        assertEquals(0, logReselectedCount)
        assertEquals(1, otherReselectedCount)
    }

    @Test
    fun `別タブに切り替えてもreselectedコールバックは呼ばれない`() {
        var readoutReselectedCount = 0
        var logReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onReadoutTabReselected = { readoutReselectedCount++ },
                onLogTabReselected = { logReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()
        rule.onNode(hasText("ルール")).performClick()
        rule.waitForIdle()

        assertEquals(0, readoutReselectedCount)
        assertEquals(0, logReselectedCount)
        assertEquals(0, otherReselectedCount)
    }

    @Test
    fun `ログタブを選択するとtelemetryLogContentが表示される`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                telemetryLogContent = { _, _ -> Text("TelemetryLogContent") },
            )
        }

        rule.onNode(hasText("ログ")).performClick()
        rule.waitForIdle()

        rule.onNodeWithText("TelemetryLogContent").assertExists()
    }

    @Test
    fun `telemetryLogContentのonFeedbackClickを呼ぶとその他タブに切り替わりonFeedbackClickにログIDが渡される`() {
        var feedbackClickedLogId: Long? = null

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onFeedbackClick = { feedbackClickedLogId = it },
                telemetryLogContent = { _, onFeedbackClick ->
                    Text(
                        text = "TelemetryLogContent",
                        modifier = Modifier.clickable(onClick = { onFeedbackClick(42L) }),
                    )
                },
                otherContent = { Text("OtherContent") },
            )
        }

        rule.onNode(hasText("ログ")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("TelemetryLogContent").performClick()
        rule.waitForIdle()

        assertEquals(42L, feedbackClickedLogId)
        rule.onNodeWithText("OtherContent").assertExists()
    }

    @Test
    fun `dynamicColorEnabledがtrueでもJVMではフォールバックのテーマで描画される`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                dynamicColorEnabled = true,
            )
        }

        rule.onNode(hasText("その他")).assertExists()
    }

    @Test
    fun `hapticFeedbackEnabledがtrueの場合タップ時のハプティックが伝播する`() {
        val fakeHaptic = FakeHapticFeedback()

        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides fakeHaptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    hapticFeedbackEnabled = true,
                    otherContent = { _ ->
                        val haptic = LocalHapticFeedback.current
                        Text(
                            text = "OtherContent",
                            modifier =
                                Modifier.clickable {
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.ContextClick,
                                    )
                                },
                        )
                    },
                )
            }
        }

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("OtherContent").performClick()
        rule.waitForIdle()

        assertEquals(listOf(HapticFeedbackType.ContextClick), fakeHaptic.performedTypes)
    }

    @Test
    fun `hapticFeedbackEnabledがfalseの場合タップ時のハプティックが伝播しない`() {
        val fakeHaptic = FakeHapticFeedback()

        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides fakeHaptic) {
                AppScreenContent(
                    layoutType = NavigationSuiteType.NavigationBar,
                    hapticFeedbackEnabled = false,
                    otherContent = { _ ->
                        val haptic = LocalHapticFeedback.current
                        Text(
                            text = "OtherContent",
                            modifier =
                                Modifier.clickable {
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.ContextClick,
                                    )
                                },
                        )
                    },
                )
            }
        }

        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("OtherContent").performClick()
        rule.waitForIdle()

        assertEquals(emptyList(), fakeHaptic.performedTypes)
    }

    @Test
    fun `selectedSimulatorIdがlmu_windowsの場合LMUが表示される`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                selectedSimulatorId = "lmu_windows",
            )
        }

        rule.onNode(hasText("LMU")).assertExists()
    }

    @Test
    fun `選択中項目をタップするとシミュレータ選択メニューが開く`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                selectedSimulatorId = "lmu_windows",
            )
        }

        rule.onNode(hasText("LMU")).performClick()
        rule.waitForIdle()

        rule.onNode(hasText("Gran Turismo 7（PS5）")).assertExists()
    }

    @Test
    fun `メニューでシミュレータを選択するとonSimulatorSelectedにIDが渡される`() {
        var selectedId: String? = null

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                selectedSimulatorId = "lmu_windows",
                onSimulatorSelected = { selectedId = it },
            )
        }

        rule.onNode(hasText("LMU")).performClick()
        rule.waitForIdle()
        rule.onNode(hasText("Gran Turismo 7（PS5）")).performClick()
        rule.waitForIdle()

        assertEquals("gt7_ps5", selectedId)
    }
}
