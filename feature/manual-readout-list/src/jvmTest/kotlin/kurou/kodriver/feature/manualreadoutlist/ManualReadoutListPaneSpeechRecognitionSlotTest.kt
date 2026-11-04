package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
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

        rule.onNodeWithText("音声認識スロット").assertDoesNotExist()
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
