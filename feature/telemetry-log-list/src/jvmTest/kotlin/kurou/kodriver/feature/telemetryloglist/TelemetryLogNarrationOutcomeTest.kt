package kurou.kodriver.feature.telemetryloglist

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import kurou.kodriver.domain.model.NarrationOutcome
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/** 読み上げ結果（[NarrationOutcome]）ごとの一覧行の表示を検証する。 */
class TelemetryLogNarrationOutcomeTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `キューに追加されたログはキュー追加アイコンを表示する`() {
        setContentWith(NarrationOutcome.QUEUED)

        rule.onNodeWithContentDescription("キューに追加").assertExists()
        rule.onNodeWithContentDescription("割り込み再生").assertDoesNotExist()
        rule.onNodeWithContentDescription("読み上げなし").assertDoesNotExist()
    }

    @Test
    fun `割り込み再生されたログは割り込み再生アイコンを表示する`() {
        setContentWith(NarrationOutcome.INTERRUPTED)

        rule.onNodeWithContentDescription("割り込み再生").assertExists()
        rule.onNodeWithContentDescription("キューに追加").assertDoesNotExist()
        rule.onNodeWithContentDescription("読み上げなし").assertDoesNotExist()
    }

    @Test
    fun `読み上げされなかったログは読み上げなしアイコンを表示する`() {
        setContentWith(NarrationOutcome.SKIPPED)

        rule.onNodeWithContentDescription("読み上げなし").assertExists()
        rule.onNodeWithContentDescription("キューに追加").assertDoesNotExist()
        rule.onNodeWithContentDescription("割り込み再生").assertDoesNotExist()
    }

    @Test
    fun `applySkippedAlphaはSKIPPEDのときに前景色を減光する`() {
        assertEquals(
            Color.Red.copy(alpha = SKIPPED_CONTENT_ALPHA),
            Color.Red.applySkippedAlpha(NarrationOutcome.SKIPPED),
        )
    }

    @Test
    fun `applySkippedAlphaはQUEUEDのときに前景色を変えない`() {
        assertEquals(Color.Red, Color.Red.applySkippedAlpha(NarrationOutcome.QUEUED))
    }

    @Test
    fun `applySkippedAlphaはINTERRUPTEDのときに前景色を変えない`() {
        assertEquals(Color.Red, Color.Red.applySkippedAlpha(NarrationOutcome.INTERRUPTED))
    }

    private fun setContentWith(narrationOutcome: NarrationOutcome) {
        rule.setContent {
            TelemetryLogListPane(
                uiState =
                    TelemetryLogListUiState(
                        logs = listOf(createTelemetryLog(id = 1, narrationOutcome = narrationOutcome)),
                    ),
            )
        }
    }
}
