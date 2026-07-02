package kurou.kodriver.feature.telemetryloglist

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.domain.model.TelemetryLog
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class TelemetryLogContentTest {

    @get:Rule
    val rule = createComposeRule()

    private val compactWindowSizeClass = WindowSizeClass.compute(400f, 800f)

    private val singlePaneDirective = PaneScaffoldDirective(
        maxHorizontalPartitions = 1,
        horizontalPartitionSpacerSize = 0.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 0.dp,
        defaultPanePreferredWidth = 360.dp,
        excludedBounds = emptyList(),
    )

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

    @Test
    fun `詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() {
        var backEnabled = false
        var capturedOnBack: (() -> Unit)? = null

        rule.setContent {
            var selectedLogId by remember { mutableStateOf<Long?>(null) }
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(selectedLogId = selectedLogId),
                onLogSelected = { selectedLogId = it },
                onClearSelectedLog = { selectedLogId = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
                detailContent = { id ->
                    Text("selected: $id")
                },
            )
        }

        assertFalse(backEnabled)

        rule.onNodeWithText("vehicle_approach").performClick()
        rule.waitUntil { backEnabled }
        rule.onNodeWithText("selected: 2").assertExists()
        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        assertFalse(backEnabled)
        rule.onNodeWithText("vehicle_approach").assertExists()
    }

    @Test
    fun `先頭から離れているときに新しいログが追加されると先頭へ戻るボタンを表示する`() {
        val logs = mutableStateOf(createTelemetryLogs())

        rule.setContent {
            TelemetryLogListPane(
                uiState = TelemetryLogListUiState(logs = logs.value),
            )
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("log_20"))
        rule.runOnIdle {
            logs.value = listOf(createTelemetryLog(id = 100, readoutItemKey = "new_log")) + logs.value
        }

        rule.onNodeWithText("新しいログ").assertExists()

        rule.onNodeWithText("新しいログ").performClick()

        rule.onNodeWithText("new_log").assertExists()
    }
}

private fun createTelemetryLogs(): List<TelemetryLog> = (30 downTo 1).map { id ->
    createTelemetryLog(id = id.toLong(), readoutItemKey = "log_$id")
}

private fun createTelemetryLog(
    id: Long,
    readoutItemKey: String,
) = TelemetryLog(
    id = id,
    createdAt = id,
    simulatorId = "lmu_windows",
    readoutItemKey = readoutItemKey,
    telemetryJson = """{"id":$id}""",
)
