package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutVehicleApproachDetailPaneTest {
    private val leftChangedTexts = mutableListOf<String>()
    private val rightChangedTexts = mutableListOf<String>()
    private val leftPreviewTexts = mutableListOf<String>()
    private val rightPreviewTexts = mutableListOf<String>()
    private val leftChanged: (String) -> Unit = { leftChangedTexts += it }
    private val rightChanged: (String) -> Unit = { rightChangedTexts += it }
    private val leftPreview: (String) -> Unit = { leftPreviewTexts += it }
    private val rightPreview: (String) -> Unit = { rightPreviewTexts += it }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ヘルプボタンをタップするとヘルプシートが表示される`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(),
                )
            }
        }

        rule.onNode(hasContentDescription("閾値の説明を表示")).performClick()

        rule.onNode(hasText("閾値は自車中心から相手車両中心までの距離です", substring = true)).assertIsDisplayed()
    }

    @Test
    fun `接近開始時は設定された左右文言を表示する`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleApproachDetailUiState(
                            startLeftText = "左注意",
                            startRightText = "右注意",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }

        rule.onNode(hasText("左注意") and hasSetTextAction()).assertExists()
        rule.onNode(hasText("右注意") and hasSetTextAction()).assertExists()
    }

    @Test
    fun `縦方向スライダーの値を確定するとonLongitudinalThresholdChangedが呼ばれる`() {
        var changedValue: Double? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(longitudinalThresholdMeters = 1.0),
                    onLongitudinalThresholdChanged = { changedValue = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 1.0f, range = 0.1f..10f, steps = 98)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }

        assertEquals(5.0, changedValue)
    }

    @Test
    fun `横方向スライダーの値を確定するとonLateralThresholdChangedが呼ばれる`() {
        var changedValue: Double? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(lateralThresholdMeters = 5.0),
                    onLateralThresholdChanged = { changedValue = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 5.0f, range = 2f..8f, steps = 59)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }

        assertEquals(4.0, changedValue)
    }

    @Test
    fun `継続接近時間スライダーの値を確定するとonSustainedApproachDurationSecondsChangedが呼ばれる`() {
        var changedValue: Int? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(sustainedApproachDurationSeconds = 4),
                    onSustainedApproachDurationSecondsChanged = { changedValue = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 4.0f, range = 4f..10f, steps = 5)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(8f) }

        assertEquals(8, changedValue)
    }

    @Test
    fun `1周目の読み上げスキップスイッチをタップするとonSkipFirstLapChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(skipFirstLap = true),
                    onSkipFirstLapChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNode(hasContentDescription("フォーメーションラップ・1周目スキップ")).performClick()

        assertEquals(false, changedEnabled)
    }

    @Test
    fun `接近開始時の読み上げスイッチをタップするとonStartReadoutEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(startReadoutEnabled = false),
                    onStartReadoutEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNode(hasText("接近開始時の読み上げ")).performClick()

        assertEquals(true, changedEnabled)
    }

    @Test
    fun `接近継続時の読み上げスイッチをタップするとonSustainedReadoutEnabledChangedが呼ばれる`() {
        var changedEnabled: Boolean? = null
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(sustainedReadoutEnabled = false),
                    onSustainedReadoutEnabledChanged = { changedEnabled = it },
                )
            }
        }

        rule.onNode(hasText("接近継続時の読み上げ")).performScrollTo().performClick()

        assertEquals(true, changedEnabled)
    }

    @Test
    fun `接近継続時は設定された左右文言を表示する`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleApproachDetailUiState(
                            sustainedLeftText = "左継続",
                            sustainedRightText = "右継続",
                        ),
                )
            }
        }
        rule.onNode(hasText("左継続・右継続")).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `縦方向閾値のリセットボタンをタップするとonResetLongitudinalThresholdが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(longitudinalThresholdMeters = 8.0),
                    onResetLongitudinalThreshold = { resetCalled = true },
                )
            }
        }

        rule.onAllNodes(hasContentDescription("デフォルトに戻す"))[0].performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `横方向閾値のリセットボタンをタップするとonResetLateralThresholdが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(lateralThresholdMeters = 7.0),
                    onResetLateralThreshold = { resetCalled = true },
                )
            }
        }

        rule.onAllNodes(hasContentDescription("デフォルトに戻す"))[1].performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `継続接近時間のリセットボタンをタップするとonResetSustainedApproachDurationSecondsが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(sustainedApproachDurationSeconds = 8),
                    onResetSustainedApproachDurationSeconds = { resetCalled = true },
                )
            }
        }

        rule.onAllNodes(hasContentDescription("デフォルトに戻す"))[2].performScrollTo().performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `左右入力は開始スイッチOFFでも編集と個別試聴ができる`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleApproachDetailUiState(
                            startReadoutEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onStartLeftTextChanged = leftChanged,
                    onStartRightTextChanged = rightChanged,
                    onStartLeftTextPreviewClicked = leftPreview,
                    onStartRightTextPreviewClicked = rightPreview,
                )
            }
        }
        rule.onAllNodes(hasSetTextAction()).assertCountEquals(2)
        rule
            .onAllNodes(hasSetTextAction())[0]
            .performScrollTo()
            .assertIsEnabled()
            .performTextReplacement("左注意")
        rule.onAllNodes(hasContentDescription("入力した文言を再生"))[0].performScrollTo().performClick()
        rule
            .onAllNodes(hasSetTextAction())[1]
            .performScrollTo()
            .assertIsEnabled()
            .performTextReplacement("右注意")
        rule.onAllNodes(hasContentDescription("入力した文言を再生"))[1].performScrollTo().performClick()
        assertEquals(listOf("左注意"), leftChangedTexts)
        assertEquals(listOf("右注意"), rightChangedTexts)
        assertEquals(listOf("左注意"), leftPreviewTexts)
        assertEquals(listOf("右注意"), rightPreviewTexts)
    }

    @Test
    fun `空欄案内を表示し30文字に制限して入力中の文言を試聴する`() {
        val longText = "あ".repeat(31)
        val savedText = "あ".repeat(30)
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutVehicleApproachDetailUiState(
                            startLeftText = "",
                            isTextToSpeechAvailable = true,
                        ),
                    onStartLeftTextChanged = leftChanged,
                    onStartRightTextChanged = rightChanged,
                    onStartLeftTextPreviewClicked = leftPreview,
                    onStartRightTextPreviewClicked = rightPreview,
                )
            }
        }
        rule.onNode(hasText("空欄のままなら読み上げません")).assertExists()
        rule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextReplacement(longText)
        rule.onAllNodes(hasContentDescription("入力した文言を再生"))[0].performScrollTo().performClick()
        rule.onNode(hasText("30/30")).assertExists()
        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement("")
        rule.onAllNodes(hasContentDescription("入力した文言を再生"))[1].performScrollTo().performClick()
        assertEquals(listOf(savedText), leftChangedTexts)
        assertEquals(listOf(savedText), leftPreviewTexts)
        assertEquals(listOf(""), rightChangedTexts)
        assertEquals(listOf(""), rightPreviewTexts)
    }

    @Test
    fun `TTS利用不可では左右の入力と試聴を無効にし案内を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(isTextToSpeechAvailable = false),
                )
            }
        }
        rule.onNode(hasText("カーレフト") and isNotEnabled()).assertExists()
        rule.onNode(hasText("カーライト") and isNotEnabled()).assertExists()
        rule.onAllNodes(hasContentDescription("入力した文言を再生"))[0].assertIsNotEnabled()
        rule.onAllNodes(hasContentDescription("入力した文言を再生"))[1].assertIsNotEnabled()
        rule
            .onAllNodes(hasText("この端末では音声合成を利用できないため、接近開始時は読み上げません"))
            .assertCountEquals(2)
    }
}
