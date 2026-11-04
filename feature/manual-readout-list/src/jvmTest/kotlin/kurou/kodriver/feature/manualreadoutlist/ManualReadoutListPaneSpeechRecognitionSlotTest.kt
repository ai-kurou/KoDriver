package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test

class ManualReadoutListPaneSpeechRecognitionSlotTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `一覧は音声認識のスロットを表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = ManualReadoutListUiState(),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                    speechRecognitionSection = { Text("音声認識スロット") },
                )
            }
        }

        rule.onNodeWithText("音声認識スロット").assertExists()
    }

    @Test
    fun `スロットを渡さない一覧には音声認識を表示しない`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = ManualReadoutListUiState(),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }

        rule.onNodeWithText("音声認識(テスト)").assertDoesNotExist()
    }

    @Test
    fun `認識履歴が長くてもセット一覧を画面内に表示する`() {
        val longHistory = List(SPEECH_RECOGNITION_HISTORY_MAX) { index -> List(5) { "とても長い認識結果の候補${index}_$it" } }
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 420.dp)) {
                    ManualReadoutListPaneContent(
                        uiState =
                            ManualReadoutListUiState(
                                sets = listOf(ManualReadoutSet(id = "set-1", name = "燃料セット")),
                            ),
                        simulatorId = "lmu_windows",
                        simulatorName = "LMU",
                        onAddSet = {},
                        onReadSet = {},
                        speechRecognitionSection = {
                            SpeechRecognitionCard(
                                uiState =
                                    SpeechRecognitionUiState(
                                        enabled = true,
                                        status = SpeechRecognitionStatus.Listening,
                                        partialText = "認識途中の長い文字列".repeat(5),
                                        recognized = longHistory,
                                    ),
                                onEnabledChange = {},
                            )
                        },
                    )
                }
            }
        }

        rule.onNodeWithText("燃料セット").assertIsDisplayed()
    }

    @Test
    fun `一覧と詳細の足場は音声認識のスロットを一覧へ渡す`() {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    ManualReadoutContentScaffold(
                        uiState = ManualReadoutListUiState(),
                        simulatorId = "lmu_windows",
                        simulatorName = "LMU",
                        onAddSet = {},
                        onReadSet = {},
                        onSetClick = {},
                        onClearSelectedSet = {},
                        speechRecognitionSection = { Text("音声認識スロット") },
                        detailContent = { _, _, _ -> },
                    )
                }
            }
        }

        rule.onNodeWithText("音声認識スロット").assertExists()
    }
}
