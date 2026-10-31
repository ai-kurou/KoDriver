package kurou.kodriver.feature.gt7ps5autoreadout.remainingfuellapsdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.buildlogic.screenshottest.assertEditableTextEquals
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

private const val DEFAULT_TEXT = GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT

@Suppress("TooManyFunctions")
class Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneContent()
            }
        }

        rule.onNodeWithText("読み上げる文言は下の欄で設定できます。", substring = true).assertIsDisplayed()
    }

    @Test
    fun `警告カードと文言入力欄と閾値置換のヒントが表示される`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNodeWithText("燃料残り周回数").assertIsDisplayed()
        rule.onNodeWithText("1周以上のときの文言").assertIsDisplayed()
        rule.onNodeWithText("燃料は残り約{laps}周").assertIsDisplayed()
        rule.onNodeWithText("{laps} は残り周回数に置き換わります").assertIsDisplayed()
        rule.onNodeWithText("{laps}を挿入").assertIsEnabled()
        rule.onNodeWithText("燃料がありません(1.0周未満)のときの文言").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("燃料残り1周未満").assertIsDisplayed()
        rule.onNodeWithText("残り約: 3 周").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState(
                            enabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = { changed += it },
                    onReadoutTextPreviewClicked = { text, laps ->
                        previews += text
                        assertEquals(3, laps)
                    },
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].assertIsEnabled().performTextReplacement("あ".repeat(31))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `未知プレースホルダーの警告が表示される`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneContent(
                    uiState =
                        Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState(
                            readoutText = "{lap}{x}{lap}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{lap}、{x} は置き換えられません。{laps} を使用してください").assertExists()
    }

    @Test
    fun `スライダーは1から5周の範囲で変更とリセットを通知する`() {
        var changed: Int? = null
        var reset = false
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = 4),
                    onRemainingFuelLapsChanged = { changed = it },
                    onResetRemainingFuelLaps = { reset = true },
                )
            }
        }
        rule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(4f, 1f..5f, 3)))
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }
        assertEquals(5, changed)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[2].performScrollTo().performClick()
        assertEquals(true, reset)
    }

    @Test
    fun `スイッチをタップするとonEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState(enabled = true),
                    onEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNodeWithText("燃料残り周回数").performClick()

        assertEquals(false, changedEnabled)
    }

    @Test
    fun `編集後のリセットは既定文言を表示して保存を通知する`() {
        val changes = mutableListOf<String>()
        setResetTestContent(changes = changes)

        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement("編集済み文言")
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsEnabled().performClick()
        rule.onAllNodes(hasSetTextAction())[0].assertEditableTextEquals(DEFAULT_TEXT)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled()
        assertEquals(listOf("編集済み文言", DEFAULT_TEXT), changes)
    }

    @Test
    fun `燃料なし用の欄は編集と試聴とリセットを通知しプレースホルダー警告を表示しない`() {
        val changes = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
                    onEmptyReadoutTextChanged = { changes += it },
                    onEmptyReadoutTextPreviewClicked = { previews += it },
                )
            }
        }
        rule.onNodeWithText("燃料がありません(1.0周未満)のときの文言").performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].assertIsNotEnabled().performClick()
        rule.onAllNodes(hasSetTextAction())[1].performTextReplacement("あ".repeat(31))
        rule.onAllNodes(hasSetTextAction())[1].assertEditableTextEquals("あ".repeat(30))
        rule.onAllNodes(hasSetTextAction())[1].performTextReplacement("燃料なし{x}")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].performClick()
        rule.onNodeWithText("{x} は置き換えられません。{laps} を使用してください").assertDoesNotExist()
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].performClick()
        rule.onAllNodes(hasSetTextAction())[1].assertEditableTextEquals("燃料残り1周未満")
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].assertIsNotEnabled()
        assertEquals(listOf("あ".repeat(30), "燃料なし{x}", "燃料残り1周未満"), changes)
        assertEquals(listOf("燃料なし{x}"), previews)
    }

    @Test
    fun `TTS不可では燃料なし用の欄の入力と試聴とリセットを無効にする`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState(emptyReadoutText = "編集済み燃料なし文言"),
                )
            }
        }
        rule.onNodeWithText("編集済み燃料なし文言").assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].assertIsNotEnabled()
    }

    private fun setResetTestContent(
        changes: MutableList<String>,
        initialState: Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState =
            Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
    ) {
        var uiState by mutableStateOf(initialState)
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5AutoReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = uiState,
                    onReadoutTextChanged = {
                        changes += it
                        uiState = uiState.copy(readoutText = it)
                    },
                )
            }
        }
    }
}
