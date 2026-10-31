package kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.assertEditableTextEquals
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

private const val DEFAULT_TEXT = LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT

@Suppress("TooManyFunctions")
class LmuWindowsAutoReadoutRemainingFuelDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `対象クラスの見出しと燃料を使用する4クラスが対象として表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNodeWithText("対象クラス").performScrollTo().assertIsDisplayed()
        listOf(
            "Hypercar、対象外",
            "LMP2、対象",
            "ELMS LMP2、対象",
            "LMP3、対象",
            "LMGTE、対象",
            "LMGT3、対象外",
        ).forEach { description ->
            rule.onNodeWithContentDescription(description).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun `タイトルと入力欄の間に4dpを確保する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutRemainingFuelDetailUiState(isTextToSpeechAvailable = true),
                    modifier = Modifier.requiredSize(360.dp, 4000.dp),
                )
            }
        }

        val expected = with(rule.density) { KoDriverSpacing.extraSmall.toPx() }
        val fields = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().map { it.boundsInRoot }
        listOf(
            "残量が閾値以下になったときの文言",
        ).forEach { label ->
            rule.onAllNodesWithText(label).assertCountEquals(1)
            rule.onAllNodesWithText(label).fetchSemanticsNodes().forEach { node ->
                val labelBounds = node.boundsInRoot
                val fieldBounds = fields.first { it.top >= labelBounds.bottom }
                assertEquals(expected, fieldBounds.top - labelBounds.bottom, absoluteTolerance = 1f)
            }
        }
    }

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNodeWithText("燃料残量が設定した閾値以下になった場合に音声でお知らせします。\n読み上げる文言は下の欄で設定できます。").assertIsDisplayed()
    }

    @Test
    fun `警告カードと文言入力欄と閾値置換のヒントが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutRemainingFuelDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNodeWithText("残量警告").assertIsDisplayed()
        rule.onNodeWithText("残量が閾値以下になったときの文言").assertIsDisplayed()
        rule.onNodeWithText(DEFAULT_TEXT).assertIsDisplayed()
        rule.onNodeWithText("{percent} は残量閾値(%)に置き換わります").assertIsDisplayed()
        rule.onNodeWithText("{percent}を挿入").assertIsEnabled()
    }

    @Test
    fun `スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutRemainingFuelDetailUiState(
                            enabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = { changed += it },
                    onReadoutTextPreviewClicked = { previews += it },
                )
            }
        }
        rule.onNode(hasSetTextAction()).assertIsEnabled().performTextReplacement("あ".repeat(31))
        rule.onNodeWithContentDescription("入力した文言を再生").performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `未知プレースホルダーの警告が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutRemainingFuelDetailUiState(
                            readoutText = "{lap}{x}{lap}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{lap}、{x} は置き換えられません。{percent} を使用してください").assertExists()
    }

    @Test
    fun `閾値のサブタイトルと説明とデフォルト値のスライダーラベルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNodeWithText("残量閾値").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("残量が30%以下になると警告を読み上げます。").assertIsDisplayed()
        rule.onNodeWithText("30%").assertIsDisplayed()
    }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとonThresholdResetが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutRemainingFuelDetailUiState(thresholdPercentage = 50),
                    onThresholdReset = { resetCalled = true },
                )
            }
        }

        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].performScrollTo().performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `スイッチをタップするとonEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutRemainingFuelDetailUiState(enabled = true),
                    onEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNodeWithText("残量警告").performClick()

        assertEquals(false, changedEnabled)
    }

    @Test
    fun `編集後のリセットは既定文言を表示して保存を通知する`() {
        val changes = mutableListOf<String>()
        setResetTestContent(changes = changes)

        rule.onNode(hasSetTextAction()).performTextReplacement("編集済み文言")
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsEnabled().performClick()
        rule.onNode(hasSetTextAction()).assertEditableTextEquals(DEFAULT_TEXT)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled()
        assertEquals(listOf("編集済み文言", DEFAULT_TEXT), changes)
    }

    private fun setResetTestContent(
        changes: MutableList<String>,
        initialState: LmuWindowsAutoReadoutRemainingFuelDetailUiState =
            LmuWindowsAutoReadoutRemainingFuelDetailUiState(isTextToSpeechAvailable = true),
    ) {
        var uiState by mutableStateOf(initialState)
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutRemainingFuelDetailPaneContent(
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
