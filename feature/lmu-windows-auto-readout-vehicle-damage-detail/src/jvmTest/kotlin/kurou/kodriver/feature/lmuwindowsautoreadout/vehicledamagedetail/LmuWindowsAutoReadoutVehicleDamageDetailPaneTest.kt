package kurou.kodriver.feature.lmuwindowsautoreadout.vehicledamagedetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
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
class LmuWindowsAutoReadoutVehicleDamageDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと入力欄の間に4dpを確保する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutVehicleDamageDetailUiState(isTextToSpeechAvailable = true),
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
                LmuWindowsAutoReadoutVehicleDamageDetailPaneContent(LmuWindowsAutoReadoutVehicleDamageDetailUiState())
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
    fun `3イベントのスイッチを切り替えられる`() {
        val overheatEnabled = mutableListOf<Boolean>()
        val partDetachedEnabled = mutableListOf<Boolean>()
        val tyreDetachedEnabled = mutableListOf<Boolean>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutVehicleDamageDetailUiState(),
                    onOverheatEnabledChanged = { overheatEnabled += it },
                    onPartDetachedEnabledChanged = { partDetachedEnabled += it },
                    onTyreDetachedEnabledChanged = { tyreDetachedEnabled += it },
                )
            }
        }
        rule.onAllNodes(hasText("オーバーヒート") and !hasSetTextAction())[0].performScrollTo().performClick()
        rule
            .onAllNodes(hasText("部品脱落（ホイール除く）") and !hasSetTextAction())[0]
            .performScrollTo()
            .performClick()
        rule.onAllNodes(hasText("タイヤ脱落") and !hasSetTextAction())[0].performScrollTo().performClick()
        assertEquals(listOf(false), overheatEnabled)
        assertEquals(listOf(false), partDetachedEnabled)
        assertEquals(listOf(false), tyreDetachedEnabled)
    }

    @Test
    fun `スイッチOFFでも3イベントの文言を上限まで切り詰めて試聴する`() {
        val overheatChanged = mutableListOf<String>()
        val overheatPreviews = mutableListOf<String>()
        val partDetachedChanged = mutableListOf<String>()
        val partDetachedPreviews = mutableListOf<String>()
        val tyreDetachedChanged = mutableListOf<String>()
        val tyreDetachedPreviews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutVehicleDamageDetailUiState(
                            overheatEnabled = false,
                            partDetachedEnabled = false,
                            tyreDetachedEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { overheatChanged += it },
                    onOverheatReadoutTextPreviewClicked = { overheatPreviews += it },
                    onPartDetachedReadoutTextChanged = { partDetachedChanged += it },
                    onPartDetachedReadoutTextPreviewClicked = { partDetachedPreviews += it },
                    onTyreDetachedReadoutTextChanged = { tyreDetachedChanged += it },
                    onTyreDetachedReadoutTextPreviewClicked = { tyreDetachedPreviews += it },
                )
            }
        }
        val expectedText = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)
        repeat(3) { index ->
            rule
                .onAllNodes(hasSetTextAction())[index]
                .performScrollTo()
                .assertIsEnabled()
                .performTextReplacement("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1))
            rule.onAllNodesWithContentDescription("入力した文言を再生")[index].performClick()
        }
        assertEquals(listOf(expectedText), overheatChanged)
        assertEquals(overheatChanged, overheatPreviews)
        assertEquals(listOf(expectedText), partDetachedChanged)
        assertEquals(partDetachedChanged, partDetachedPreviews)
        assertEquals(listOf(expectedText), tyreDetachedChanged)
        assertEquals(tyreDetachedChanged, tyreDetachedPreviews)
    }

    @Test
    fun `3イベントの文言をデフォルトに戻せる`() {
        val overheatChanged = mutableListOf<String>()
        val partDetachedChanged = mutableListOf<String>()
        val tyreDetachedChanged = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutVehicleDamageDetailUiState(
                            overheatReadoutText = "カスタム",
                            partDetachedReadoutText = "カスタム",
                            tyreDetachedReadoutText = "カスタム",
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { overheatChanged += it },
                    onPartDetachedReadoutTextChanged = { partDetachedChanged += it },
                    onTyreDetachedReadoutTextChanged = { tyreDetachedChanged += it },
                )
            }
        }
        repeat(3) { index ->
            rule
                .onAllNodesWithContentDescription("デフォルトに戻す")[index]
                .performScrollTo()
                .assertIsEnabled()
                .performClick()
            rule.onAllNodesWithContentDescription("デフォルトに戻す")[index].assertIsNotEnabled()
        }
        assertEquals(listOf("オーバーヒート"), overheatChanged)
        assertEquals(listOf("部品脱落"), partDetachedChanged)
        assertEquals(listOf("タイヤ脱落"), tyreDetachedChanged)
    }

    @Test
    fun `3イベントとも古い保存値を無視し正規化済みの保存値で待機を解除する`() {
        var overheatSavedText by mutableStateOf("オーバーヒート")
        var partDetachedSavedText by mutableStateOf("部品脱落")
        var tyreDetachedSavedText by mutableStateOf("タイヤ脱落")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutVehicleDamageDetailUiState(
                            overheatReadoutText = overheatSavedText,
                            partDetachedReadoutText = partDetachedSavedText,
                            tyreDetachedReadoutText = tyreDetachedSavedText,
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextReplacement(" あい ")
        rule.runOnIdle { overheatSavedText = "あ" }
        rule.onAllNodes(hasSetTextAction())[0].assert(hasText(" あい "))
        rule.runOnIdle { overheatSavedText = "あい" }
        rule.waitForIdle()
        rule.runOnIdle { overheatSavedText = "外部更新" }
        rule.onAllNodes(hasSetTextAction())[0].assert(hasText("外部更新"))

        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement(" あい ")
        rule.runOnIdle { partDetachedSavedText = "あ" }
        rule.onAllNodes(hasSetTextAction())[1].assert(hasText(" あい "))
        rule.runOnIdle { partDetachedSavedText = "あい" }
        rule.waitForIdle()
        rule.runOnIdle { partDetachedSavedText = "外部更新" }
        rule.onAllNodes(hasSetTextAction())[1].assert(hasText("外部更新"))

        rule.onAllNodes(hasSetTextAction())[2].performScrollTo().performTextReplacement(" あい ")
        rule.runOnIdle { tyreDetachedSavedText = "あ" }
        rule.onAllNodes(hasSetTextAction())[2].assert(hasText(" あい "))
        rule.runOnIdle { tyreDetachedSavedText = "あい" }
        rule.waitForIdle()
        rule.runOnIdle { tyreDetachedSavedText = "外部更新" }
        rule.onAllNodes(hasSetTextAction())[2].assert(hasText("外部更新"))
    }

    @Test
    fun `空欄ではスキップの説明を表示しリセットが有効になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutVehicleDamageDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutVehicleDamageDetailUiState(
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
                LmuWindowsAutoReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutVehicleDamageDetailUiState(),
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
