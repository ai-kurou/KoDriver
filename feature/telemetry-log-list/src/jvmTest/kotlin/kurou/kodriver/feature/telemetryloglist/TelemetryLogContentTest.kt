package kurou.kodriver.feature.telemetryloglist

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class TelemetryLogContentTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `一覧ペインにログを表示する`() {
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState,
            )
        }

        rule.onNodeWithText("flag").assertExists()
        rule.onNodeWithText("vehicle_approach").assertExists()
        rule.onNodeWithText("remaining_fuel_laps").assertExists()
    }

    @Test
    fun `ログが0件の場合は空状態を表示する`() {
        rule.setContent {
            TelemetryLogContentScaffold()
        }

        rule.onNodeWithText("ログはまだありません").assertExists()
        rule.onNodeWithText("テレメトリを受信すると、ここに新しい順で表示されます。").assertExists()
    }

    @Test
    fun `ログをタップするとdetailPaneにログIDを渡す`() {
        rule.setContent {
            var selectedLogId by remember { mutableStateOf<Long?>(null) }
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(selectedLogId = selectedLogId),
                onLogSelected = { selectedLogId = it },
                detailContent = { id ->
                    Text("selected: $id")
                },
            )
        }

        rule.onNodeWithText("vehicle_approach").performClick()

        rule.onNodeWithText("selected: 2").assertExists()
    }
}
