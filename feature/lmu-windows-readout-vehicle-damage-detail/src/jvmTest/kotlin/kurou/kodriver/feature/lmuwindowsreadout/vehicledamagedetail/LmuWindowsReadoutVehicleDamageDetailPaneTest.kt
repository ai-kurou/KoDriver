package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

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
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("TooManyFunctions")
class LmuWindowsReadoutVehicleDamageDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと入力欄の間に4dpを確保する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(isTextToSpeechAvailable = true),
                    modifier = Modifier.requiredSize(360.dp, 4000.dp),
                )
            }
        }

        val expected = with(rule.density) { KoDriverSpacing.extraSmall.toPx() }
        val fields = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().map { it.boundsInRoot }
        listOf(
            "読み上げ文言",
        ).forEach { label ->
            rule.onAllNodesWithText(label).assertCountEquals(3)
            rule.onAllNodesWithText(label).fetchSemanticsNodes().forEach { node ->
                val labelBounds = node.boundsInRoot
                val fieldBounds = fields.first { it.top >= labelBounds.bottom }
                assertEquals(expected, fieldBounds.top - labelBounds.bottom, absoluteTolerance = 1f)
            }
        }
    }

    @Test
    fun `説明文と3カードを表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(LmuWindowsReadoutVehicleDamageDetailUiState())
            }
        }
        rule
            .onNodeWithText("車両の故障状況を音声でお知らせします。\n読み上げる文言は下の欄で設定できます。")
            .assertIsDisplayed()
        listOf("オーバーヒート", "部品脱落（ホイール除く）", "タイヤ脱落").forEach {
            rule.onAllNodes(hasText(it) and !hasSetTextAction())[0].performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun `オーバーヒートスイッチを切り替えられる`() {
        var enabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(),
                    onOverheatEnabledChanged = { enabled = it },
                )
            }
        }
        rule.onAllNodes(hasText("オーバーヒート") and !hasSetTextAction())[0].performScrollTo().performClick()
        assertEquals(false, enabled)
    }

    @Test
    fun `オーバーヒートはスイッチOFFでも文言を編集し上限まで切り詰めた文言を試聴する`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            overheatEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changed += it },
                    onOverheatReadoutTextPreviewClicked = { previews += it },
                )
            }
        }
        rule
            .onAllNodes(hasSetTextAction())[0]
            .performScrollTo()
            .assertIsEnabled()
            .performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
        assertEquals(listOf("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `オーバーヒート文言をデフォルトに戻せる`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            overheatReadoutText = "カスタム",
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changed += it },
                )
            }
        }
        rule
            .onAllNodesWithContentDescription("デフォルトに戻す")[0]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf("オーバーヒート"), changed)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled()
    }

    @Test
    fun `オーバーヒートは古い保存値を無視し正規化済みの保存値で待機を解除する`() {
        var savedText by mutableStateOf("オーバーヒート")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            overheatReadoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextReplacement(" あい ")
        rule.runOnIdle { savedText = "あ" }
        rule.onNode(hasSetTextAction() and hasText(" あい ")).assertExists()
        rule.runOnIdle { savedText = "あい" }
        rule.waitForIdle()
        rule.runOnIdle { savedText = "外部更新" }
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }

    @Test
    fun `部品脱落スイッチを切り替えられる`() {
        var enabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(),
                    onPartDetachedEnabledChanged = { enabled = it },
                )
            }
        }
        rule.onAllNodes(hasText("部品脱落（ホイール除く）") and !hasSetTextAction())[0].performScrollTo().performClick()
        assertEquals(false, enabled)
    }

    @Test
    fun `部品脱落はスイッチOFFでも文言を編集し上限まで切り詰めた文言を試聴する`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            partDetachedEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onPartDetachedReadoutTextChanged = { changed += it },
                    onPartDetachedReadoutTextPreviewClicked = { previews += it },
                )
            }
        }
        rule
            .onAllNodes(hasSetTextAction())[1]
            .performScrollTo()
            .assertIsEnabled()
            .performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].performClick()
        assertEquals(listOf("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `部品脱落文言をデフォルトに戻せる`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            partDetachedReadoutText = "カスタム",
                            isTextToSpeechAvailable = true,
                        ),
                    onPartDetachedReadoutTextChanged = { changed += it },
                )
            }
        }
        rule
            .onAllNodesWithContentDescription("デフォルトに戻す")[1]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf("部品脱落"), changed)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].assertIsNotEnabled()
    }

    @Test
    fun `部品脱落は古い保存値を無視し正規化済みの保存値で待機を解除する`() {
        var savedText by mutableStateOf("部品脱落")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            partDetachedReadoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement(" あい ")
        rule.runOnIdle { savedText = "あ" }
        rule.onNode(hasSetTextAction() and hasText(" あい ")).assertExists()
        rule.runOnIdle { savedText = "あい" }
        rule.waitForIdle()
        rule.runOnIdle { savedText = "外部更新" }
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }

    @Test
    fun `タイヤ脱落スイッチを切り替えられる`() {
        var enabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(),
                    onTyreDetachedEnabledChanged = { enabled = it },
                )
            }
        }
        rule.onAllNodes(hasText("タイヤ脱落") and !hasSetTextAction())[0].performScrollTo().performClick()
        assertEquals(false, enabled)
    }

    @Test
    fun `タイヤ脱落はスイッチOFFでも文言を編集し上限まで切り詰めた文言を試聴する`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            tyreDetachedEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onTyreDetachedReadoutTextChanged = { changed += it },
                    onTyreDetachedReadoutTextPreviewClicked = { previews += it },
                )
            }
        }
        rule
            .onAllNodes(hasSetTextAction())[2]
            .performScrollTo()
            .assertIsEnabled()
            .performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[2].performClick()
        assertEquals(listOf("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `タイヤ脱落文言をデフォルトに戻せる`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            tyreDetachedReadoutText = "カスタム",
                            isTextToSpeechAvailable = true,
                        ),
                    onTyreDetachedReadoutTextChanged = { changed += it },
                )
            }
        }
        rule
            .onAllNodesWithContentDescription("デフォルトに戻す")[2]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf("タイヤ脱落"), changed)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[2].assertIsNotEnabled()
    }

    @Test
    fun `タイヤ脱落は古い保存値を無視し正規化済みの保存値で待機を解除する`() {
        var savedText by mutableStateOf("タイヤ脱落")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            tyreDetachedReadoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[2].performScrollTo().performTextReplacement(" あい ")
        rule.runOnIdle { savedText = "あ" }
        rule.onNode(hasSetTextAction() and hasText(" あい ")).assertExists()
        rule.runOnIdle { savedText = "あい" }
        rule.waitForIdle()
        rule.runOnIdle { savedText = "外部更新" }
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }

    @Test
    fun `空欄ではスキップの説明を表示しリセットが有効になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleDamageDetailUiState(
                            overheatReadoutText = "",
                            partDetachedReadoutText = "",
                            tyreDetachedReadoutText = "",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        repeat(3) { index ->
            rule.onAllNodesWithText("空欄のままなら読み上げません")[index].performScrollTo().assertIsDisplayed()
            rule.onAllNodesWithContentDescription("デフォルトに戻す")[index].assertIsEnabled()
        }
    }

    @Test
    fun `TTS利用不可では文言入力と試聴とリセットが無効になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(),
                )
            }
        }
        repeat(3) { index ->
            rule.onAllNodesWithContentDescription("入力した文言を再生")[index].assertIsNotEnabled()
            rule.onAllNodesWithContentDescription("デフォルトに戻す")[index].assertIsNotEnabled()
            rule
                .onAllNodesWithText("この端末では音声合成を利用できないため、読み上げません")[index]
                .assertIsDisplayed()
        }
    }
}
