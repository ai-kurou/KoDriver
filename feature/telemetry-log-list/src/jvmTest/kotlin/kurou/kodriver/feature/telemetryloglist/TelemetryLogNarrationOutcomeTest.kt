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
    fun `キューに追加されたログはキュー追加アイコンだけを表示する`() {
        assertOnlyIconShown(NarrationOutcome.QUEUED, "キューに追加")
    }

    @Test
    fun `通常再生されたログは通常再生アイコンだけを表示する`() {
        assertOnlyIconShown(NarrationOutcome.SPOKEN, "通常再生")
    }

    @Test
    fun `割り込み再生されたログは割り込み再生アイコンだけを表示する`() {
        assertOnlyIconShown(NarrationOutcome.INTERRUPTED, "割り込み再生")
    }

    @Test
    fun `読み上げされなかったログは読み上げなしアイコンだけを表示する`() {
        assertOnlyIconShown(NarrationOutcome.SKIPPED, "読み上げなし")
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

    @Test
    fun `applySkippedAlphaはSPOKENのときに前景色を変えない`() {
        assertEquals(Color.Red, Color.Red.applySkippedAlpha(NarrationOutcome.SPOKEN))
    }

    /** [narrationOutcome] の行に [expectedDescription] のアイコンだけが表示されることを検証する。 */
    private fun assertOnlyIconShown(
        narrationOutcome: NarrationOutcome,
        expectedDescription: String,
    ) {
        rule.setContent {
            TelemetryLogListPane(
                uiState =
                    TelemetryLogListUiState(
                        logs = listOf(createTelemetryLog(id = 1, narrationOutcome = narrationOutcome)),
                    ),
            )
        }

        rule.onNodeWithContentDescription(expectedDescription).assertExists()
        allDescriptions
            .filterNot { it == expectedDescription }
            .forEach { rule.onNodeWithContentDescription(it).assertDoesNotExist() }
    }

    private companion object {
        val allDescriptions = listOf("キューに追加", "通常再生", "割り込み再生", "読み上げなし")
    }
}
