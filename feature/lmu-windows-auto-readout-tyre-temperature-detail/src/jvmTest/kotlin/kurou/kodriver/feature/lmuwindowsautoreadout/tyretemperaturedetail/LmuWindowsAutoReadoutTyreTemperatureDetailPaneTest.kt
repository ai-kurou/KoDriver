package kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
class LmuWindowsAutoReadoutTyreTemperatureDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()

    @Test
    fun `過熱警告OFFでは車両クラスの選択も触覚も通知しない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        val selections = mutableListOf<LmuWindowsVehicleClassData>()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                        uiState =
                            LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                                overheatWarningEnabled = false,
                                vehicleClassHighThresholdCelsius = mapOf(LmuWindowsVehicleClassData.Gt3 to 95),
                            ),
                        onVehicleClassSelected = { selections += it },
                    )
                }
            }
        }

        rule.onNodeWithContentDescription("LMGT3、95°C").performScrollTo().assertIsNotEnabled()

        assertEquals(emptyList(), selections)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `低温警告の各フェーズを選択解除するたびに一度触覚を通知する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        val phases =
            listOf(
                SessionPhase.GARAGE to "ガレージ",
                SessionPhase.WARM_UP to "ウォームアップラップ",
                SessionPhase.GRID_WALK to "グリッドウォーク",
                SessionPhase.FORMATION to "フォーメーションラップ",
            )
        var selectedPhases by mutableStateOf(emptySet<SessionPhase>())
        val toggles = mutableListOf<SessionPhase>()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                        uiState = LmuWindowsAutoReadoutTyreTemperatureDetailUiState(lowWarningPhases = selectedPhases),
                        onLowWarningPhaseToggled = { phase ->
                            toggles += phase
                            selectedPhases =
                                if (phase in selectedPhases) {
                                    selectedPhases - phase
                                } else {
                                    selectedPhases + phase
                                }
                        },
                    )
                }
            }
        }

        phases.forEach { (_, label) ->
            rule
                .onNodeWithText(label)
                .performScrollTo()
                .assertIsNotSelected()
                .performClick()
                .assertIsSelected()
            rule.onNodeWithText(label).performClick().assertIsNotSelected()
        }

        assertEquals(phases.flatMap { (phase, _) -> listOf(phase, phase) }, toggles)
        verify(exactly = 8) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `タイトルと入力欄の間に4dpを確保する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutTyreTemperatureDetailUiState(isTextToSpeechAvailable = true),
                    modifier = Modifier.requiredSize(360.dp, 4000.dp),
                )
            }
        }

        val expected = with(rule.density) { KoDriverSpacing.extraSmall.toPx() }
        val fields = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().map { it.boundsInRoot }
        listOf(
            "タイヤが過熱したときの文言",
            "タイヤが冷えているときの文言",
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
    fun `リセットボタンをクリックするとonVehicleClassHighThresholdResetが選択中クラスで呼ばれる`() {
        var resetVehicleClass: LmuWindowsVehicleClassData? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius = mapOf(LmuWindowsVehicleClassData.Hypercar to 95),
                        ),
                    onVehicleClassHighThresholdReset = { resetVehicleClass = it },
                )
            }
        }
        rule.onNodeWithContentDescription("デフォルトに戻す").performScrollTo().performClick()
        assertEquals(LmuWindowsVehicleClassData.Hypercar, resetVehicleClass)
    }

    @Test
    fun `ヘルプボタンをタップするとヘルプシートが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutTyreTemperatureDetailUiState(),
                )
            }
        }

        rule.onNodeWithContentDescription("高温閾値の説明を表示").performClick()

        rule.onNodeWithText("設定した温度以上になると過熱警告を読み上げます", substring = true).assertIsDisplayed()
    }

    @Test
    fun `スライダーの値を確定するとonVehicleClassHighThresholdChangedが選択中クラスで呼ばれる`() {
        var changedVehicleClass: LmuWindowsVehicleClassData? = null
        var changedValue: Int? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius = mapOf(LmuWindowsVehicleClassData.Hypercar to 90),
                        ),
                    onVehicleClassHighThresholdChanged = { vehicleClass, celsius ->
                        changedVehicleClass = vehicleClass
                        changedValue = celsius
                    },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 90f, range = 90f..110f, steps = 19)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(95f) }

        assertEquals(LmuWindowsVehicleClassData.Hypercar, changedVehicleClass)
        assertEquals(95, changedValue)
    }

    @Test
    fun `スライダーは選択中クラスの閾値を表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 90,
                                    LmuWindowsVehicleClassData.Gt3 to 97,
                                ),
                            selectedVehicleClass = LmuWindowsVehicleClassData.Gt3,
                        ),
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 97f, range = 90f..110f, steps = 19)),
            ).performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `過熱警告カードのヘッダーをタップするとonOverheatWarningEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutTyreTemperatureDetailUiState(overheatWarningEnabled = true),
                    onOverheatWarningEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNodeWithText("過熱警告").performClick()

        assertEquals(false, changedEnabled)
    }

    @Test
    fun `低温文言入力欄が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutTyreTemperatureDetailUiState(lowWarningEnabled = true),
                )
            }
        }

        rule.onAllNodesWithText("タイヤが冷えているときの文言", substring = true)[0].performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `対象クラスのサブタイトルとクラス別閾値のチップが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Gt3 to 90,
                                    LmuWindowsVehicleClassData.Unknown("") to 95,
                                ),
                        ),
                )
            }
        }

        rule.onNodeWithText("対象クラス").assertIsDisplayed()
        rule.onNodeWithText("90°C").assertIsDisplayed()
        rule.onNodeWithContentDescription("LMGT3、90°C").assertIsDisplayed()
    }

    @Test
    fun `対象クラスの画像タイルはHypercarがデフォルトで選択され温度を読み上げる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 90,
                                    LmuWindowsVehicleClassData.Gt3 to 95,
                                ),
                        ),
                )
            }
        }

        rule.onNodeWithContentDescription("Hypercar、90°C").assertIsSelected()
        rule.onNodeWithContentDescription("LMGT3、95°C").assertIsNotSelected()
        rule.onNodeWithText("90°C").assertIsDisplayed()
        rule.onNodeWithText("95°C").performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithText("Hypercar").assertCountEquals(0)
    }

    @Test
    fun `対象クラスの画像タイルは選択済みのselectedVehicleClassが選択状態になる`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 90,
                                    LmuWindowsVehicleClassData.Gt3 to 95,
                                ),
                            selectedVehicleClass = LmuWindowsVehicleClassData.Gt3,
                        ),
                )
            }
        }

        rule.onNodeWithContentDescription("LMGT3、95°C").assertIsSelected()
        rule.onNodeWithContentDescription("Hypercar、90°C").assertIsNotSelected()
    }

    @Test
    fun `対象クラスの画像タイルは狭い幅ではFlowRowで折り返す`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            vehicleClassHighThresholdCelsius =
                                mapOf(
                                    LmuWindowsVehicleClassData.Hypercar to 100,
                                    LmuWindowsVehicleClassData.P2 to 90,
                                    LmuWindowsVehicleClassData.P2Elms to 90,
                                    LmuWindowsVehicleClassData.P3 to 90,
                                    LmuWindowsVehicleClassData.Gte to 100,
                                    LmuWindowsVehicleClassData.Gt3 to 90,
                                ),
                        ),
                    modifier = Modifier.requiredSize(250.dp, 2500.dp),
                )
            }
        }

        val firstTile = rule.onNodeWithContentDescription("Hypercar、100°C").fetchSemanticsNode().boundsInRoot
        val thirdTile = rule.onNodeWithContentDescription("ELMS LMP2、90°C").fetchSemanticsNode().boundsInRoot

        assertEquals(firstTile.left, thirdTile.left, absoluteTolerance = 1f)
        assertTrue(thirdTile.top > firstTile.top)
    }

    @Test
    fun `対象クラスのチップをクリックするとonVehicleClassSelectedにそのクラスが渡される`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        var selectedVehicleClass: LmuWindowsVehicleClassData? = null
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                        uiState =
                            LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                                vehicleClassHighThresholdCelsius =
                                    mapOf(
                                        LmuWindowsVehicleClassData.Hypercar to 90,
                                        LmuWindowsVehicleClassData.Gt3 to 95,
                                    ),
                            ),
                        onVehicleClassSelected = { selectedVehicleClass = it },
                    )
                }
            }
        }

        rule.onNodeWithContentDescription("LMGT3、95°C").performScrollTo().performClick()

        assertEquals(LmuWindowsVehicleClassData.Gt3, selectedVehicleClass)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `過熱文言の試聴は選択中クラスの現在の閾値を渡す`() {
        var selectedClass by mutableStateOf<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
        val thresholds = mapOf<LmuWindowsVehicleClassData, Int>(LmuWindowsVehicleClassData.Gt3 to 107)
        val previews = mutableListOf<Pair<String, Int>>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            isTextToSpeechAvailable = true,
                            vehicleClassHighThresholdCelsius = thresholds,
                            selectedVehicleClass = selectedClass,
                        ),
                    onOverheatReadoutTextPreviewClicked = { text, celsius -> previews += text to celsius },
                )
            }
        }
        val expected = mutableListOf<Pair<String, Int>>()
        tyreTemperatureVehicleClasses.forEach { vehicleClass ->
            selectedClass = vehicleClass
            rule.waitForIdle()
            rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
            val celsius =
                thresholds[vehicleClass]
                    ?: lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass).value
            expected += LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT to celsius
        }
        assertEquals(expected, previews)
    }

    @Test
    fun `スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            overheatWarningEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { changed += it },
                    onOverheatReadoutTextPreviewClicked = { text, _ -> previews += text },
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[0].assertIsEnabled().performTextReplacement("あ".repeat(31))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `低温 スイッチOFFでも入力中の文言を編集して試聴できる`() {
        val changed = mutableListOf<String>()
        val previews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            lowWarningEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onColdReadoutTextChanged = { changed += it },
                    onLowWarningPreviewClicked = { previews += it },
                )
            }
        }
        rule
            .onAllNodes(hasSetTextAction())[1]
            .performScrollTo()
            .assertIsEnabled()
            .performTextReplacement("あ".repeat(31))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].performScrollTo().performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
    }

    @Test
    fun `低温 前後に空白がある入力も保存済みの正規化後の文言と一致したら同期する`() {
        var savedText by mutableStateOf("")
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            coldReadoutText = savedText,
                            isTextToSpeechAvailable = true,
                        ),
                    onColdReadoutTextChanged = {},
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement(" あい ")
        savedText = "あい"
        rule.waitForIdle()
        savedText = "う"
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("う")).assertExists()
    }

    @Test
    fun `低温文言の編集とリセットは過熱文言を変更しない`() {
        val coldChanges = mutableListOf<String>()
        val overheatChanges = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "冷やして",
                            coldReadoutText = "温めて",
                            isTextToSpeechAvailable = true,
                        ),
                    onColdReadoutTextChanged = { coldChanges += it },
                    onOverheatReadoutTextChanged = { overheatChanges += it },
                )
            }
        }
        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement("低温注意")
        rule.onNodeWithContentDescription("低温警告の文言をデフォルトに戻す").performScrollTo().performClick()
        assertEquals(listOf("低温注意", LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT), coldChanges)
        assertEquals(emptyList<String>(), overheatChanges)
        rule.onNode(hasSetTextAction() and hasText("冷やして")).assertExists()
        rule
            .onNode(hasSetTextAction() and hasText(LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT))
            .assertExists()
    }

    @Test
    fun `温度チップは入力末尾に追加して過熱と低温の保存コールバックへ渡す`() {
        val overheatChanges = mutableListOf<String>()
        val coldChanges = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "過熱",
                            coldReadoutText = "低温",
                            isTextToSpeechAvailable = true,
                        ),
                    onOverheatReadoutTextChanged = { overheatChanges += it },
                    onColdReadoutTextChanged = { coldChanges += it },
                )
            }
        }
        rule.onAllNodesWithText("{celsius}を挿入")[0].performScrollTo().performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText("{celsius}を挿入")[1].performScrollTo().performClick()
        rule.waitForIdle()
        assertEquals(listOf("過熱{celsius}"), overheatChanges)
        assertEquals(listOf("低温{celsius}"), coldChanges)
        rule.onNode(hasSetTextAction() and hasText("過熱{celsius}")).assertExists()
        rule.onNode(hasSetTextAction() and hasText("低温{celsius}")).assertExists()
    }

    @Test
    fun `未知のプレースホルダーは出現順で重複なく警告し既知の温度は警告しない`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                    uiState =
                        LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                            overheatReadoutText = "{celsius}{wheel}{x}{wheel}",
                            coldReadoutText = "{celsius}{wheel}{x}{wheel}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        listOf(0, 1).forEach { index ->
            rule
                .onAllNodesWithText("未対応のプレースホルダー {wheel}、{x} はそのまま読み上げられます")[index]
                .performScrollTo()
                .assertIsDisplayed()
        }
        rule.onAllNodesWithText("未対応のプレースホルダー {celsius}", substring = true).assertCountEquals(0)
    }
}
