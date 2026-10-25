package kurou.kodriver.feature.telemetryloglist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/** 読み上げ経路ごとのバッジとヘルプを検証する。 */
class TelemetryLogNarrationSourceTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `自動読み上げのログは自動アイコンだけを表示する`() {
        assertOnlySourceIconShown(NarrationSource.AUTO, "自動読み上げ")
    }

    @Test
    fun `手動読み上げのログは手動アイコンだけを表示する`() {
        assertOnlySourceIconShown(NarrationSource.MANUAL, "手動読み上げ")
    }

    @Test
    fun `選択中の読み上げなしログでも自動バッジを表示する`() {
        assertOnlySourceIconShown(NarrationSource.AUTO, "自動読み上げ", isSelected = true, outcome = NarrationOutcome.SKIPPED)
    }

    @Test
    fun `選択中の読み上げなしログでも手動バッジを表示する`() {
        assertOnlySourceIconShown(
            NarrationSource.MANUAL,
            "手動読み上げ",
            isSelected = true,
            outcome = NarrationOutcome.SKIPPED,
        )
    }

    @Test
    fun `narrationSourceIconは2種の経路に対応するアイコンを返す`() {
        assertEquals(Icons.Default.HeadsetMic, narrationSourceIcon(NarrationSource.AUTO))
        assertEquals(Icons.Default.TouchApp, narrationSourceIcon(NarrationSource.MANUAL))
    }

    @Test
    fun `読み上げ結果ヘルプから自動と手動の説明を確認できる`() {
        rule.setContent {
            KoDriverTheme {
                TelemetryLogListPane(uiState = TelemetryLogListUiState(logs = listOf(createTelemetryLog(id = 1))))
            }
        }

        rule.onNodeWithContentDescription("読み上げ結果の見方を表示").performClick()
        rule.onNodeWithText("自動読み上げ").performScrollTo().assertIsDisplayed()
        rule
            .onNodeWithText("走行状況に応じて自動で判定され、読み上げが要求されました。")
            .performScrollTo()
            .assertIsDisplayed()
        rule.onNodeWithText("手動読み上げ").performScrollTo().assertIsDisplayed()
        rule
            .onNodeWithText("手動読み上げタブの操作やホットキーで、読み上げが要求されました。")
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun assertOnlySourceIconShown(
        source: NarrationSource,
        expectedDescription: String,
        isSelected: Boolean = false,
        outcome: NarrationOutcome = NarrationOutcome.SPOKEN,
    ) {
        rule.setContent {
            KoDriverTheme {
                TelemetryLogListPane(
                    uiState =
                        TelemetryLogListUiState(
                            logs =
                                listOf(
                                    createTelemetryLog(id = 1, narrationOutcome = outcome, narrationSource = source),
                                ),
                            selectedLogId = if (isSelected) 1L else null,
                        ),
                )
            }
        }

        rule.onNodeWithContentDescription(expectedDescription).assertIsDisplayed()
        listOf("自動読み上げ", "手動読み上げ")
            .filterNot { it == expectedDescription }
            .forEach { rule.onNodeWithContentDescription(it).assertDoesNotExist() }
    }
}
