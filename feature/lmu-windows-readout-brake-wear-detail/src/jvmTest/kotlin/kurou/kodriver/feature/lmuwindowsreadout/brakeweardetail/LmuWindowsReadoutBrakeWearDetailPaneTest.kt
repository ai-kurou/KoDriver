package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
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
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

private const val DEFAULT_TEXT = LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT

@Suppress("TooManyFunctions")
class LmuWindowsReadoutBrakeWearDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと入力欄の間に4dpを確保する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
                    modifier = Modifier.requiredSize(360.dp, 4000.dp),
                )
            }
        }

        val expected = with(rule.density) { KoDriverSpacing.extraSmall.toPx() }
        val fields = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().map { it.boundsInRoot }
        listOf(
            "ブレーキ残量が閾値以下になったときの文言",
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
                LmuWindowsReadoutBrakeWearDetailPaneContent()
            }
        }

        rule
            .onNodeWithText(
                "ブレーキの残量が設定した閾値以下になった際に音声でお知らせします。" +
                    "一度警告を読み上げた後は、ブレーキを交換して全輪の残量が閾値を超えるまで再度読み上げません。\n" +
                    "読み上げる文言は下の欄で設定できます。\n" +
                    "残量はLMUのREST APIから取得するため、デスクトップ版でLMUを起動しているときのみ取得できます。" +
                    "新品時の厚さは取得できないため、観測した最大の厚さを100%とします。" +
                    "摩耗した状態で観測を始めた場合、実際より多く表示されます。",
            ).assertIsDisplayed()
    }

    @Test
    fun `警告カードと文言入力欄と閾値置換のヒントが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNodeWithText("残量警告").assertIsDisplayed()
        rule.onNodeWithText("ブレーキ残量が閾値以下になったときの文言").assertIsDisplayed()
        rule.onNodeWithText("ブレーキ残量{percent}%以下").assertIsDisplayed()
        rule.onNodeWithText("{percent} は選択中の車両クラスの残量閾値(%)に置き換わります").assertIsDisplayed()
        rule.onNodeWithText("{percent}を挿入").assertIsEnabled()
    }

    @Test
    fun `スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
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
    fun `保存済みの古い文言が流れてきても入力中の文言を巻き戻さず一致したら同期する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            readoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = {},
                )
            }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement("あい")
        savedText = "あ"
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("あい")).assertExists()
        savedText = "あい"
        rule.waitForIdle()
        savedText = "う"
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("う")).assertExists()
    }

    @Test
    fun `前後に空白を含む入力はtrim後の保存値で待機を解除し以降の更新を反映する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            readoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = {},
                )
            }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement(" あい ")
        rule.runOnIdle { savedText = "あい" }
        rule.waitForIdle()
        rule.runOnIdle { savedText = "外部更新" }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }

    @Test
    fun `最大長超過入力は制限後の保存値で待機を解除し以降の更新を反映する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            readoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = {},
                )
            }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement("あ".repeat(31))
        rule.runOnIdle { savedText = "あ".repeat(30) }
        rule.waitForIdle()
        rule.runOnIdle { savedText = "外部更新" }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }

    @Test
    fun `入力直後の文言にpercentを挿入し上限ちょうどで無効になる`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
                    onReadoutTextChanged = { changed += it },
                )
            }
        }
        val text = "あ".repeat(21)
        rule.onNode(hasSetTextAction()).performTextReplacement(text)
        rule.onNodeWithText("{percent}を挿入").assertIsEnabled().performClick()
        assertEquals(listOf(text, text + "{percent}"), changed)
        rule.onNodeWithText("{percent}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `挿入で文字数上限を超える場合は無効になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            readoutText = "あ".repeat(22),
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{percent}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `未知プレースホルダーの警告が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            readoutText = "{lap}{x}{lap}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{lap}、{x} は置き換えられません。{percent} を使用してください").assertExists()
    }

    @Test
    fun `空欄では読み上げない案内を表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            readoutText = "",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
        rule.onNodeWithContentDescription("入力した文言を再生").assertIsEnabled()
    }

    @Test
    fun `TTS不可では利用不可案内を優先し入力と試聴と挿入を無効にする`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeWearDetailUiState(readoutText = "{lap}"),
                )
            }
        }
        rule.onNodeWithText("{lap}").assertIsNotEnabled()
        rule.onNodeWithText("{percent}を挿入").assertIsNotEnabled()
        rule.onNodeWithContentDescription("入力した文言を再生").assertIsNotEnabled()
        rule.onNodeWithText("この端末では音声合成を利用できないため、読み上げません").assertExists()
    }

    @Test
    fun `残量閾値のサブタイトルと説明とデフォルト値のスライダーラベルが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent()
            }
        }

        rule.onNodeWithText("残量閾値").assertIsDisplayed()
        rule.onNodeWithText("いずれかのブレーキの残量が20%以下になると警告を読み上げます。").assertIsDisplayed()
        rule.onNodeWithText("20%").assertIsDisplayed()
    }

    @Test
    fun `デフォルト値から変更している場合にリセットボタンをタップするとonThresholdResetが呼ばれる`() {
        var resetVehicleClass: LmuWindowsVehicleClassData? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            vehicleClassLowThresholdPercent = mapOf(LmuWindowsVehicleClassData.Hypercar to 10),
                            selectedVehicleClass = LmuWindowsVehicleClassData.Hypercar,
                        ),
                    onThresholdReset = { resetVehicleClass = it },
                )
            }
        }

        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].performScrollTo().performClick()

        assertEquals(LmuWindowsVehicleClassData.Hypercar, resetVehicleClass)
    }

    @Test
    fun `スイッチをタップするとonEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeWearDetailUiState(enabled = true),
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

    @Test
    fun `既定文言と同じならリセットできず保存も通知しない`() {
        val changes = mutableListOf<String>()
        setResetTestContent(changes = changes)

        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled().performClick()
        assertEquals(emptyList(), changes)
    }

    @Test
    fun `TTS利用不可では編集済み文言をリセットできず保存も通知しない`() {
        val changes = mutableListOf<String>()
        setResetTestContent(
            changes = changes,
            initialState = LmuWindowsReadoutBrakeWearDetailUiState(readoutText = "編集済み文言"),
        )

        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled().performClick()
        rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText)).assertEditableTextEquals("編集済み文言")
        assertEquals(emptyList(), changes)
    }

    @Test
    fun `保存待ちのリセットは古い文言で巻き戻らず保存完了後は同期する`() {
        val changes = mutableListOf<String>()
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            readoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onReadoutTextChanged = { changes += it },
                )
            }
        }

        rule.onNode(hasSetTextAction()).performTextReplacement("編集済み文言")
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].performClick()
        rule.onNode(hasSetTextAction()).assertEditableTextEquals(DEFAULT_TEXT)
        rule.runOnIdle { savedText = "編集済み文言" }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction()).assertEditableTextEquals(DEFAULT_TEXT)
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].assertIsNotEnabled()
        rule.runOnIdle { savedText = DEFAULT_TEXT }
        rule.waitForIdle()
        rule.runOnIdle { savedText = "保存後の変更" }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction()).assertEditableTextEquals("保存後の変更")
        assertEquals(listOf("編集済み文言", DEFAULT_TEXT), changes)
    }

    @Test
    fun `対象クラスを選択するとそのクラスを通知し未知クラスは表示しない`() {
        var selected: LmuWindowsVehicleClassData? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            vehicleClassLowThresholdPercent =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 20,
                                    LmuWindowsVehicleClassData.Gte to 12,
                                    LmuWindowsVehicleClassData.Unknown("custom") to 40,
                                ),
                        ),
                    onVehicleClassSelected = { selected = it },
                )
            }
        }
        rule.onNodeWithText("GTE（12%）").performScrollTo().performClick()
        assertEquals(LmuWindowsVehicleClassData.Gte, selected)
        rule.onNodeWithText("custom（40%）").assertDoesNotExist()
    }

    private fun setResetTestContent(
        changes: MutableList<String>,
        initialState: LmuWindowsReadoutBrakeWearDetailUiState =
            LmuWindowsReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
    ) {
        var uiState by mutableStateOf(initialState)
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState = uiState,
                    onReadoutTextChanged = {
                        changes += it
                        uiState = uiState.copy(readoutText = it)
                    },
                )
            }
        }
    }

    private fun SemanticsNodeInteraction.assertEditableTextEquals(expected: String) =
        assert(
            SemanticsMatcher("EditableText == $expected") {
                it.config.getOrNull(SemanticsProperties.EditableText)?.text == expected
            },
        )
}
