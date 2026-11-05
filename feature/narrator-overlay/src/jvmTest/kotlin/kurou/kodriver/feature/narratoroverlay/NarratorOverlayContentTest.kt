package kurou.kodriver.feature.narratoroverlay

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

private const val FADE_MIDWAY_MILLIS = 75L
private const val FADE_COMPLETE_MILLIS = 300L

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
        rule.runOnIdle { state.value = NarratorOverlayUiState(latestTelemetryLog = telemetryLog("新")) }
        rule.mainClock.advanceTimeBy(FADE_MIDWAY_MILLIS)
        rule.onAllNodesWithText("旧").assertCountEquals(1)
        rule.onAllNodesWithText("新").assertCountEquals(1)
        rule.mainClock.advanceTimeBy(FADE_COMPLETE_MILLIS)
        rule.onAllNodesWithText("旧").assertCountEquals(0)
        rule.onAllNodesWithText("新").assertCountEquals(1)
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
