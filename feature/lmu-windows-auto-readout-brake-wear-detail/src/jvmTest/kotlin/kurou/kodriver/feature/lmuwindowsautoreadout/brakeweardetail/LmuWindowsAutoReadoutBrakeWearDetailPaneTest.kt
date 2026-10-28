package kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.buildlogic.screenshottest.assertEditableTextEquals
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

private const val DEFAULT_TEXT = LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT

@Suppress("TooManyFunctions")
class LmuWindowsAutoReadoutBrakeWearDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()

    @Test
    fun `文言の中間のカーソル位置にpercentを挿入して保存通知する`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
                    onReadoutTextChanged = { changed += it },
                )
            }
        }
        val text = "前半後半"
        rule.onNode(hasSetTextAction()).performTextReplacement(text)
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(2))
        rule.onNodeWithText("{percent}を挿入").assertIsEnabled().performClick()
        rule.waitForIdle()
        assertEquals(listOf(text, "前半{percent}後半"), changed)
    }

    @Test
    fun `タイトルと入力欄の間に4dpを確保する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent()
            }
        }

        rule
            .onNodeWithText(
                "ブレーキの残量が設定した閾値以下になった際に音声でお知らせします。" +
                    "一度警告を読み上げた後は、ブレーキを交換して全輪の残量が閾値を超えるまで再度読み上げません。\n" +
                    "読み上げる文言は下の欄で設定できます。",
            ).assertIsDisplayed()
    }

    @Test
    fun `警告カードと文言入力欄と閾値置換のヒントが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNodeWithText("ブレーキ残量").assertDoesNotExist()
        rule.onNodeWithText("取得できません").assertDoesNotExist()
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
                    onReadoutTextChanged = { changed += it },
                )
            }
        }
        val text = "あ".repeat(21)
        rule.onNode(hasSetTextAction()).performTextReplacement(text)
        rule.onNodeWithText("{percent}を挿入").assertIsEnabled().performClick()
        rule.waitForIdle()
        assertEquals(listOf(text, text + "{percent}"), changed)
        rule.onNodeWithText("{percent}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `挿入で文字数上限を超える場合は無効になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutBrakeWearDetailUiState(readoutText = "{lap}"),
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent()
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutBrakeWearDetailUiState(enabled = true),
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
            initialState = LmuWindowsAutoReadoutBrakeWearDetailUiState(readoutText = "編集済み文言"),
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
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
        rule
            .onNodeWithContentDescription("LMGTE、12%")
            .performScrollTo()
            .performClick()
        assertEquals(LmuWindowsVehicleClassData.Gte, selected)
        rule.onNodeWithContentDescription("custom、40%").assertDoesNotExist()
        rule.onNodeWithContentDescription("LMP2", substring = true).assertDoesNotExist()
    }

    @Test
    fun `対象クラスの画像タイルはHypercarがデフォルトで選択され残量閾値を読み上げる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
                            vehicleClassLowThresholdPercent =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 12,
                                    LmuWindowsVehicleClassData.Gt3 to 15,
                                ),
                        ),
                )
            }
        }

        rule.onNodeWithContentDescription("Hypercar、12%").assertIsSelected()
        rule.onNodeWithContentDescription("LMGT3、15%").assertIsNotSelected()
        rule
            .onNodeWithContentDescription("Hypercar、12%")
            .assert(hasText("12%"))
        rule
            .onNodeWithContentDescription("LMGT3、15%")
            .performScrollTo()
            .assert(hasText("15%"))
        rule.onAllNodesWithText("Hypercar").assertCountEquals(0)
    }

    @Test
    fun `対象クラスの画像タイルは選択済みのselectedVehicleClassが選択状態になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutBrakeWearDetailUiState(
                            vehicleClassLowThresholdPercent =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 12,
                                    LmuWindowsVehicleClassData.Gt3 to 15,
                                ),
                            selectedVehicleClass = LmuWindowsVehicleClassData.Gt3,
                        ),
                )
            }
        }

        rule.onNodeWithContentDescription("LMGT3、15%").assertIsSelected()
        rule.onNodeWithContentDescription("Hypercar、12%").assertIsNotSelected()
    }

    @Test
    fun `全6クラスの画像タイルの選択を通知して選択状態を更新する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        val vehicleClasses =
            listOf(
                LmuWindowsVehicleClassData.Hypercar to "Hypercar",
                LmuWindowsVehicleClassData.P2 to "LMP2",
                LmuWindowsVehicleClassData.P2Elms to "ELMS LMP2",
                LmuWindowsVehicleClassData.P3 to "LMP3",
                LmuWindowsVehicleClassData.Gte to "LMGTE",
                LmuWindowsVehicleClassData.Gt3 to "LMGT3",
            )
        val selections = mutableListOf<LmuWindowsVehicleClassData>()
        var selectedClass by mutableStateOf<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                        uiState =
                            LmuWindowsAutoReadoutBrakeWearDetailUiState(
                                selectedVehicleClass = selectedClass,
                                vehicleClassLowThresholdPercent =
                                    vehicleClasses.associate { (vehicleClass, _) ->
                                        vehicleClass to 12
                                    },
                            ),
                        onVehicleClassSelected = {
                            selections += it
                            selectedClass = it
                        },
                        modifier = Modifier.requiredWidth(360.dp),
                    )
                }
            }
        }
        vehicleClasses.forEach { (_, accessibleName) ->
            rule
                .onNodeWithContentDescription("$accessibleName、12%")
                .performScrollTo()
                .assertIsDisplayed()
                .assertIsEnabled()
                .performClick()
                .assertIsSelected()
        }
        assertEquals(vehicleClasses.map { it.first }, selections)
        verify(exactly = 6) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `警告OFFでは全6クラスの画像タイルを無効にして選択を通知しない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        val selected = mutableListOf<LmuWindowsVehicleClassData>()
        val vehicleClasses =
            listOf(
                LmuWindowsVehicleClassData.Hypercar to "Hypercar",
                LmuWindowsVehicleClassData.P2 to "LMP2",
                LmuWindowsVehicleClassData.P2Elms to "ELMS LMP2",
                LmuWindowsVehicleClassData.P3 to "LMP3",
                LmuWindowsVehicleClassData.Gte to "LMGTE",
                LmuWindowsVehicleClassData.Gt3 to "LMGT3",
            )
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                        uiState =
                            LmuWindowsAutoReadoutBrakeWearDetailUiState(
                                enabled = false,
                                vehicleClassLowThresholdPercent =
                                    vehicleClasses.associate { (vehicleClass, _) ->
                                        vehicleClass to 12
                                    },
                            ),
                        onVehicleClassSelected = { selected += it },
                        modifier = Modifier.requiredWidth(360.dp),
                    )
                }
            }
        }
        vehicleClasses.forEach { (_, accessibleName) ->
            rule
                .onNodeWithContentDescription("$accessibleName、12%")
                .performScrollTo()
                .assertIsDisplayed()
                .assertIsNotEnabled()
        }
        assertEquals(emptyList(), selected)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    private fun setResetTestContent(
        changes: MutableList<String>,
        initialState: LmuWindowsAutoReadoutBrakeWearDetailUiState =
            LmuWindowsAutoReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
    ) {
        var uiState by mutableStateOf(initialState)
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
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
