package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TelemetryLogListPaneTest {
    private val haptic: HapticFeedback = mockk()
    private val onLogClick: (Long) -> Unit = mockk()
    private val onFeedbackClick: (Long) -> Unit = mockk()
    private val onDeleteClick: (Long) -> Unit = mockk()

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `メニューからフィードバックを選ぶと触覚の後にコールバックを一度だけ呼ぶ`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        every { onFeedbackClick(1L) } returns Unit
        showPaneWithMenuCallbacks()

        rule.onNodeWithContentDescription("その他の操作").performClick()
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 0) { onFeedbackClick(1L) }

        rule.onNodeWithText("フィードバック送信").performClick()

        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 1) { onFeedbackClick(1L) }
        verify(exactly = 0) { onDeleteClick(1L) }
        verify(exactly = 0) { onLogClick(1L) }
        verifyOrder {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            onFeedbackClick(1L)
        }
        confirmVerified(haptic, onLogClick, onFeedbackClick, onDeleteClick)
    }

    @Test
    fun `メニューから削除を選ぶと触覚の後にコールバックを一度だけ呼ぶ`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        every { onDeleteClick(1L) } returns Unit
        showPaneWithMenuCallbacks()

        rule.onNodeWithContentDescription("その他の操作").performClick()
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 0) { onDeleteClick(1L) }

        rule.onNodeWithText("削除").performClick()

        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 1) { onDeleteClick(1L) }
        verify(exactly = 0) { onFeedbackClick(1L) }
        verify(exactly = 0) { onLogClick(1L) }
        verifyOrder {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            onDeleteClick(1L)
        }
        confirmVerified(haptic, onLogClick, onFeedbackClick, onDeleteClick)
    }

    @Test
    fun `選択中のシミュレーターがない場合は既存の空状態を表示する`() {
        rule.setContent {
            KoDriverTheme {
                TelemetryLogListPane(uiState = TelemetryLogListUiState(selectedSimulator = null))
            }
        }

        rule.onNodeWithText("ログはまだありません").assertIsDisplayed()
        rule.onNodeWithText("テレメトリを受信すると、ここに新しい順で表示されます。").assertIsDisplayed()
        rule.onNodeWithText("読み上げ結果").assertDoesNotExist()
    }

    @Test
    fun `ログがある場合は読み上げ結果ヒントを表示してヘルプから4種の説明を確認できる`() {
        rule.setContent {
            KoDriverTheme {
                TelemetryLogListPane(uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))))
            }
        }

        rule.onNodeWithText("読み上げ結果").assertIsDisplayed()
        rule.onNodeWithContentDescription("読み上げ結果の見方を表示").performClick()
        rule.onNodeWithText("キュー経由で再生").assertIsDisplayed()
        rule.onNodeWithText("キュー経由で読み上げを要求しました。").assertIsDisplayed()
        rule.onNodeWithText("通常再生").assertIsDisplayed()
        rule.onNodeWithText("すぐに読み上げを要求しました。").assertIsDisplayed()
        rule.onNodeWithText("割り込み再生").assertIsDisplayed()
        rule.onNodeWithText("読み上げ中の内容を止めて、優先して読み上げを要求しました。").assertIsDisplayed()
        rule.onNodeWithText("読み上げなし").assertIsDisplayed()
        rule.onNodeWithText("優先度が低い、本文が空、またはTTSが使えないため読み上げませんでした。").assertIsDisplayed()
    }

    @Test
    fun `先頭の読み上げ結果ヒントは一覧上端に配置する`() {
        rule.setContent {
            KoDriverTheme {
                TelemetryLogListPane(uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))))
            }
        }

        val listBounds = rule.onNodeWithTag(TELEMETRY_LOG_LIST_TEST_TAG).fetchSemanticsNode().boundsInRoot
        val hintBounds = rule.onNode(hasText("読み上げ結果")).fetchSemanticsNode().boundsInRoot
        val maximumTopInset = with(rule.density) { KoDriverSpacing.medium.toPx() }
        assertTrue(hintBounds.top - listBounds.top in 0f..maximumTopInset)
    }

    @Test
    fun `ヒント行表示中の新着ログは自動で先頭へ戻す`() {
        val logs = (30 downTo 1).map { createTelemetryLog(id = it.toLong()) }
        val uiState = mutableStateOf(TelemetryLogListUiState(logs = logs))
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.height(240.dp)) { TelemetryLogListPane(uiState = uiState.value) }
            }
        }

        rule.onNodeWithTag(TELEMETRY_LOG_LIST_TEST_TAG).performScrollToIndex(0)
        rule.runOnIdle { uiState.value = uiState.value.copy(logs = listOf(createTelemetryLog(id = 100)) + logs) }
        rule.onNodeWithText("新しいログ").assertDoesNotExist()
    }

    @Test
    fun `先頭ログ表示中の新着ログは先頭へ戻るボタンを表示する`() {
        val logs = (30 downTo 1).map { createTelemetryLog(id = it.toLong()) }
        val uiState = mutableStateOf(TelemetryLogListUiState(logs = logs))
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.height(240.dp)) { TelemetryLogListPane(uiState = uiState.value) }
            }
        }

        rule.onNodeWithTag(TELEMETRY_LOG_LIST_TEST_TAG).performScrollToIndex(1)
        rule.runOnIdle { uiState.value = uiState.value.copy(logs = listOf(createTelemetryLog(id = 100)) + logs) }

        rule.onNodeWithText("新しいログ").assertIsDisplayed()
    }

    @Test
    fun `選択中のシミュレーターの表示名を含む空状態を表示する`() {
        rule.setContent {
            KoDriverTheme {
                TelemetryLogListPane(uiState = TelemetryLogListUiState(selectedSimulator = Simulator.LmuWindows))
            }
        }

        rule.onNodeWithText("Le Mans Ultimate（Windows版）のログはまだありません").assertIsDisplayed()
        rule.onNodeWithText("テレメトリを受信すると、ここに新しい順で表示されます。").assertIsDisplayed()
        rule.onNodeWithText("読み上げ結果").assertDoesNotExist()
    }

    @Test
    fun `シミュレーターを切り替えると新着ログボタンを非表示にしてリストを先頭へ戻す`() {
        val logs = (30 downTo 1).map { createTelemetryLog(id = it.toLong()) }
        val uiState =
            mutableStateOf(TelemetryLogListUiState(logs = logs, selectedSimulator = Simulator.LmuWindows))
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.height(240.dp)) {
                    TelemetryLogListPane(uiState = uiState.value)
                }
            }
        }

        rule.onNodeWithTag(TELEMETRY_LOG_LIST_TEST_TAG).performScrollToIndex(15)
        rule.runOnIdle {
            uiState.value = uiState.value.copy(logs = listOf(createTelemetryLog(id = 100)) + logs)
        }
        rule.onNodeWithText("新しいログ").assertIsDisplayed()

        rule.runOnIdle {
            uiState.value =
                TelemetryLogListUiState(
                    selectedSimulator = Simulator.AceWindows,
                    logs =
                        (60 downTo 31).map {
                            createTelemetryLog(
                                id = it.toLong(),
                                simulator = Simulator.AceWindows,
                                readoutItemKey = AceWindowsReadoutItemKey.Flag.Root,
                            )
                        },
                )
        }

        rule.onNodeWithText("新しいログ").assertDoesNotExist()
        rule.onNodeWithText("読み上げ結果").assertIsDisplayed()
        rule.onNodeWithText("09:00:00.060").assertIsDisplayed()
    }

    @Test
    fun `UTC0時0分0秒はJST9時0分0秒として表示する`() {
        rule.setContent {
            KoDriverTheme {
                Text(formatTelemetryLogTime(createdAt = 0L))
            }
        }

        rule.onNodeWithText("09:00:00.000").assertIsDisplayed()
    }

    @Test
    fun `日付をまたぐ時刻もJSTの時刻として表示する`() {
        val millisecondsPerDay = 24 * 60 * 60 * 1_000L
        val fifteenHoursInMillis = 15 * 60 * 60 * 1_000L

        rule.setContent {
            KoDriverTheme {
                Text(formatTelemetryLogTime(createdAt = fifteenHoursInMillis))
            }
        }

        rule.onNodeWithText("00:00:00.000").assertIsDisplayed()
        assertEquals(0L, (fifteenHoursInMillis + 9 * 60 * 60 * 1_000L) % millisecondsPerDay)
    }

    private fun showPaneWithMenuCallbacks() {
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    TelemetryLogListPane(
                        uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))),
                        onLogClick = onLogClick,
                        onFeedbackClick = onFeedbackClick,
                        onDeleteClick = onDeleteClick,
                    )
                }
            }
        }
    }
}
