package kurou.kodriver.feature.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpeechRecognitionChipTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `停止中はOFFに分類する`() {
        assertEquals(SpeechRecognitionChipState.Off, SpeechRecognitionUiState().toChipState())
    }

    @Test
    fun `有効で準備中と待機中はONに分類する`() {
        listOf(SpeechRecognitionStatus.Starting, SpeechRecognitionStatus.Listening).forEach { status ->
            assertEquals(
                SpeechRecognitionChipState.On,
                SpeechRecognitionUiState(enabled = true, status = status).toChipState(),
            )
        }
    }

    @Test
    fun `コマンド待ちはコマンド待ちに分類する`() {
        assertEquals(
            SpeechRecognitionChipState.CommandWaiting,
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.CommandWaiting).toChipState(),
        )
    }

    @Test
    fun `聞き取れない状態はすべてエラーに分類する`() {
        listOf(
            SpeechRecognitionStatus.PermissionDenied,
            SpeechRecognitionStatus.Unavailable,
            SpeechRecognitionStatus.MicrophoneUnavailable,
            SpeechRecognitionStatus.NoSignal,
            SpeechRecognitionStatus.RecognitionFailed,
        ).forEach { status ->
            assertEquals(
                SpeechRecognitionChipState.Error,
                SpeechRecognitionUiState(enabled = true, status = status).toChipState(),
            )
        }
    }

    @Test
    fun `停止中は停止中の説明を付けて表示する`() {
        rule.setContent { KoDriverTheme { SpeechRecognitionChip(uiState = SpeechRecognitionUiState()) } }

        rule.onNodeWithContentDescription("停止中").assertIsDisplayed()
    }

    @Test
    fun `聞き取り中は呼びかけ待ちの説明を付けて表示する`() {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionChip(
                    uiState = SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Listening),
                )
            }
        }

        rule.onNodeWithContentDescription("「コドライバー」の呼びかけを待っています(マイクを使用中)").assertIsDisplayed()
    }

    @Test
    fun `コマンド待ちはコマンド聞き取り中の説明を付けて表示する`() {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionChip(
                    uiState = SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.CommandWaiting),
                )
            }
        }

        rule.onNodeWithContentDescription("コマンドを聞き取り中(マイクを使用中)").assertIsDisplayed()
    }

    @Test
    fun `エラー時は原因の説明を付けて表示する`() {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionChip(
                    uiState = SpeechRecognitionUiState(status = SpeechRecognitionStatus.PermissionDenied),
                )
            }
        }

        rule.onNodeWithContentDescription("マイクの権限が許可されていません").assertIsDisplayed()
    }

    @Test
    fun `コマンド待ちの波紋は広がるほど大きく薄くなり範囲外は端に丸める`() {
        assertEquals(0.4f, commandPulseRadiusFraction(0f))
        assertEquals(1f, commandPulseRadiusFraction(1f))
        assertEquals(0.4f, commandPulseRadiusFraction(-1f))
        assertEquals(1f, commandPulseRadiusFraction(2f))
        assertEquals(0.5f, commandPulseAlpha(0f))
        assertEquals(0f, commandPulseAlpha(1f))
        assertEquals(0.5f, commandPulseAlpha(-1f))
        assertEquals(0f, commandPulseAlpha(2f))
    }

    @Test
    fun `コマンド待ちに入ると波紋のアニメーション中も表示し続け、抜けても表示する`() {
        rule.mainClock.autoAdvance = false
        var uiState by mutableStateOf(
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Listening),
        )
        rule.setContent { KoDriverTheme { SpeechRecognitionChip(uiState = uiState) } }
        uiState = uiState.copy(status = SpeechRecognitionStatus.CommandWaiting)
        listOf(100L, 400L, 2000L).forEach { millis ->
            rule.mainClock.advanceTimeBy(millis)
            rule.onRoot().assertIsDisplayed()
        }
        uiState = uiState.copy(status = SpeechRecognitionStatus.Listening)
        rule.mainClock.advanceTimeBy(100)
        rule.onRoot().assertIsDisplayed()
    }

    @Test
    fun `音量は段階に変換し範囲外は端に丸める`() {
        assertEquals(0, volumeRingStep(0f))
        assertEquals(0, volumeRingStep(0.09f))
        assertEquals(1, volumeRingStep(0.1f))
        assertEquals(3, volumeRingStep(0.5f))
        assertEquals(VOLUME_RING_STEPS, volumeRingStep(1f))
        assertEquals(0, volumeRingStep(-1f))
        assertEquals(VOLUME_RING_STEPS, volumeRingStep(2f))
    }

    @Test
    fun `音量リングの弧の角度は段階に比例する`() {
        assertEquals(0f, volumeRingSweepAngle(0))
        assertEquals(72f, volumeRingSweepAngle(1))
        assertEquals(360f, volumeRingSweepAngle(VOLUME_RING_STEPS))
    }

    @Test
    fun `音量リングは伸びるときより縮むときをゆっくり動かす`() {
        assertEquals(80, volumeRingAnimationMillis(rising = true))
        assertEquals(250, volumeRingAnimationMillis(rising = false))
    }

    @Test
    fun `音量の増減とOFFへの切り替えをアニメーション中も表示し続ける`() {
        rule.mainClock.autoAdvance = false
        var uiState by mutableStateOf(
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Listening, level = 0f),
        )
        rule.setContent { KoDriverTheme { SpeechRecognitionChip(uiState = uiState) } }
        listOf(1f, 0.2f, 0f).forEach { level ->
            uiState = uiState.copy(level = level)
            rule.mainClock.advanceTimeBy(40)
            rule.onRoot().assertIsDisplayed()
        }
        uiState = SpeechRecognitionUiState()
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithContentDescription("停止中").assertIsDisplayed()
    }

    @Test
    fun `音量リングの段階は聞き取り中だけ求め、OFFとエラーと音量なしではnullにする`() {
        val listening =
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Listening, level = 0.5f)
        assertEquals(3, listening.volumeRingStepOrNull())
        val commandWaiting = listening.copy(status = SpeechRecognitionStatus.CommandWaiting, level = 1f)
        assertEquals(VOLUME_RING_STEPS, commandWaiting.volumeRingStepOrNull())
        assertEquals(0, listening.copy(level = 0f).volumeRingStepOrNull())
        assertNull(listening.copy(level = null).volumeRingStepOrNull())
        assertNull(listening.copy(enabled = false).volumeRingStepOrNull())
        assertNull(listening.copy(status = SpeechRecognitionStatus.PermissionDenied).volumeRingStepOrNull())
    }

    @Test
    fun `音量があっても全状態で表示できる`() {
        var uiState by mutableStateOf(SpeechRecognitionUiState())
        rule.setContent { KoDriverTheme { SpeechRecognitionChip(uiState = uiState) } }
        listOf(
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Listening, level = 0.6f),
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Listening, level = 0f),
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.CommandWaiting, level = 1f),
            SpeechRecognitionUiState(level = 0.6f),
            SpeechRecognitionUiState(status = SpeechRecognitionStatus.PermissionDenied, level = 0.6f),
        ).forEach { next ->
            uiState = next
            rule.waitForIdle()
            rule.onRoot().assertIsDisplayed()
        }
    }

    @Test
    fun `タップを受け取るとクリックを通知する`() {
        var clicks = 0
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionChip(uiState = SpeechRecognitionUiState(), onClick = { clicks++ })
            }
        }

        rule.onNodeWithContentDescription("停止中").performClick()

        rule.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    fun `クリックを渡さないときはタップできない`() {
        rule.setContent { KoDriverTheme { SpeechRecognitionChip(uiState = SpeechRecognitionUiState()) } }

        rule.onNodeWithContentDescription("停止中").assertHasNoClickAction()
    }
}
