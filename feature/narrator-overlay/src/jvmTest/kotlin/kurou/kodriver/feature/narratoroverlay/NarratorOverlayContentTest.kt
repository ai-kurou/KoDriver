package kurou.kodriver.feature.narratoroverlay

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.height
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.model.TelemetryLog
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val FADE_MIDWAY_MILLIS = 75L
private const val FADE_COMPLETE_MILLIS = 300L
private const val CENTER_TOLERANCE_DP = 1f

class NarratorOverlayContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `既定色と保存したARGB文字色をTextに適用する`() {
        val state = mutableStateOf(NarratorOverlayUiState())
        rule.setContent { NarratorOverlayContent(uiState = state.value) }
        listOf(OVERLAY_TEXT_COLOR_DEFAULT, 0x80123ABCL, 0xFFFFFFFFL).forEach { argb ->
            rule.runOnIdle { state.value = NarratorOverlayUiState(overlayTextColor = argb) }
            val results = mutableListOf<TextLayoutResult>()
            rule.onNodeWithText("").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                action(results)
            }
            assertEquals(
                Color(argb),
                results
                    .single()
                    .layoutInput.style.color,
            )
        }
    }

    @Test
    fun `読み上げ内容の更新は短いフェードで切り替わる`() {
        val state = mutableStateOf(NarratorOverlayUiState(latestTelemetryLog = telemetryLog("旧")))
        rule.setContent { NarratorOverlayContent(uiState = state.value) }
        rule.mainClock.autoAdvance = false
        rule.runOnIdle { state.value = NarratorOverlayUiState(latestTelemetryLog = telemetryLog("新しい長めの読み上げ内容")) }
        rule.mainClock.advanceTimeBy(FADE_MIDWAY_MILLIS)
        rule.onAllNodesWithText("旧").assertCountEquals(1)
        rule.onAllNodesWithText("新しい長めの読み上げ内容").assertCountEquals(1)
        val oldBounds = rule.onNodeWithText("旧").getBoundsInRoot()
        val newBounds = rule.onNodeWithText("新しい長めの読み上げ内容").getBoundsInRoot()
        assertEquals(
            (oldBounds.left.value + oldBounds.right.value) / 2,
            (newBounds.left.value + newBounds.right.value) / 2,
            CENTER_TOLERANCE_DP,
        )
        rule.mainClock.advanceTimeBy(FADE_COMPLETE_MILLIS)
        rule.onAllNodesWithText("旧").assertCountEquals(0)
        rule.onAllNodesWithText("新しい長めの読み上げ内容").assertCountEquals(1)
    }

    @Test
    fun `認識途中の文字列があればマイク文言として表示し、確定した発話は表示しない`() {
        rule.setContent {
            NarratorOverlayContent(
                uiState =
                    NarratorOverlayUiState(
                        speechRecognitionTranscript = SpeechRecognitionTranscript("ピット", "燃料"),
                    ),
            )
        }
        rule.onNodeWithText("ピット").assertIsDisplayed()
        rule.onAllNodesWithText("燃料").assertCountEquals(0)
    }

    @Test
    fun `認識途中の文字列がなければ確定した発話をマイク文言として表示する`() {
        rule.setContent {
            NarratorOverlayContent(
                uiState =
                    NarratorOverlayUiState(
                        speechRecognitionTranscript = SpeechRecognitionTranscript("", "燃料"),
                    ),
            )
        }
        rule.onNodeWithText("燃料").assertIsDisplayed()
    }

    @Test
    fun `マイク文言のunkは置換語で表示する`() {
        rule.setContent {
            NarratorOverlayContent(
                uiState =
                    NarratorOverlayUiState(
                        speechRecognitionTranscript = SpeechRecognitionTranscript("", "[unk]燃料"),
                    ),
            )
        }
        rule.onNodeWithText("？燃料").assertIsDisplayed()
        rule.onAllNodesWithText("[unk]燃料").assertCountEquals(0)
    }

    @Test
    fun `マイク文言がなければ下段を表示しない`() {
        rule.setContent {
            NarratorOverlayContent(
                uiState = NarratorOverlayUiState(latestTelemetryLog = telemetryLog("イエローフラッグ")),
            )
        }
        // 上段の読み上げ文言だけが存在する。
        rule.onNodeWithText("イエローフラッグ").assertIsDisplayed()
        rule.onAllNodesWithText("？").assertCountEquals(0)
    }

    @Test
    fun `マイク文言は読み上げ文言より小さい文字で表示する`() {
        rule.setContent {
            NarratorOverlayContent(
                uiState =
                    NarratorOverlayUiState(
                        latestTelemetryLog = telemetryLog("イエローフラッグ"),
                        speechRecognitionTranscript = SpeechRecognitionTranscript("", "燃料"),
                    ),
            )
        }
        val narratedHeight = rule.onNodeWithText("イエローフラッグ").getBoundsInRoot().height
        val transcriptHeight = rule.onNodeWithText("燃料").getBoundsInRoot().height
        assertTrue(transcriptHeight < narratedHeight)
    }

    @Test
    fun `マイク文言の表示文字列は認識途中を優先し、なければ確定した発話、どちらもなければ空になる`() {
        assertEquals("ピット", narratorOverlayTranscriptText(SpeechRecognitionTranscript("ピット", "燃料"), "？"))
        assertEquals("燃料", narratorOverlayTranscriptText(SpeechRecognitionTranscript("", "燃料"), "？"))
        assertEquals("", narratorOverlayTranscriptText(SpeechRecognitionTranscript(), "？"))
        assertEquals("？タイヤ？", narratorOverlayTranscriptText(SpeechRecognitionTranscript("[unk]タイヤ[unk]"), "？"))
    }

    private fun telemetryLog(narratedText: String) =
        TelemetryLog(
            id = 1L,
            createdAt = 1_000L,
            simulator = Simulator.AceWindows,
            readoutItemKey = AceWindowsReadoutItemKey.RemainingFuel.Root,
            narratedText = narratedText,
            narrationOutcome = NarrationOutcome.INTERRUPTED,
            narrationSource = NarrationSource.AUTO,
            telemetryJson = "{}",
        )
}
