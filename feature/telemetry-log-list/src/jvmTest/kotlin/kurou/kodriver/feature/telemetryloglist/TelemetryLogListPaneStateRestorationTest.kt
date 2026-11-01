package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test

class TelemetryLogListPaneStateRestorationTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `状態復元後も新着ログボタンの表示状態が維持される`() =
        runComposeUiTest {
            val logs = (30 downTo 1).map { createTelemetryLog(id = it.toLong()) }
            var uiState by
                mutableStateOf(TelemetryLogListUiState(logs = logs, selectedSimulator = Simulator.LmuWindows))
            var registry by mutableStateOf(SaveableStateRegistry(null) { true })
            var visible by mutableStateOf(true)
            setContent {
                if (visible) {
                    CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                        KoDriverTheme {
                            Box(modifier = Modifier.height(240.dp)) {
                                TelemetryLogListPane(uiState = uiState)
                            }
                        }
                    }
                }
            }
            onNodeWithTag(TELEMETRY_LOG_LIST_TEST_TAG).performScrollToIndex(15)
            uiState = uiState.copy(logs = listOf(createTelemetryLog(id = 100)) + logs)
            waitUntilExactlyOneExists(hasText("新しいログ"))

            val saved = registry.performSave()
            visible = false
            waitForIdle()
            registry = SaveableStateRegistry(saved) { true }
            visible = true
            waitForIdle()

            waitUntilExactlyOneExists(hasText("新しいログ"))
            onNodeWithText("新しいログ").assertIsDisplayed()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `状態復元後もログ項目のメニューの展開状態が維持される`() =
        runComposeUiTest {
            val uiState =
                TelemetryLogListUiState(
                    logs = listOf(createTelemetryLog(id = 1)),
                    selectedSimulator = Simulator.LmuWindows,
                )
            var registry by mutableStateOf(SaveableStateRegistry(null) { true })
            var visible by mutableStateOf(true)
            setContent {
                if (visible) {
                    CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                        KoDriverTheme {
                            TelemetryLogListPane(uiState = uiState)
                        }
                    }
                }
            }
            onAllNodesWithContentDescription("その他の操作")[0].performClick()
            onNodeWithText("フィードバック送信").assertIsDisplayed()

            val saved = registry.performSave()
            visible = false
            waitForIdle()
            registry = SaveableStateRegistry(saved) { true }
            visible = true
            waitForIdle()

            waitUntilExactlyOneExists(hasText("フィードバック送信"))
            onNodeWithText("フィードバック送信").assertIsDisplayed()
        }
}
