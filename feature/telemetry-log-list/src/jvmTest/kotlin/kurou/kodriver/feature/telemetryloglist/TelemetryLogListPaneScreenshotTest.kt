package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import org.junit.Test

class TelemetryLogListPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            TelemetryLogListPane(
                                uiState = previewTelemetryLogListUiState,
                            )
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `空状態`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            TelemetryLogListPane()
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `新着ログボタン表示`() =
        composeScreenshotTest {
            mainClock.autoAdvance = false
            val uiState = mutableStateOf(TelemetryLogListUiState(logs = manyTelemetryLogs))
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            TelemetryLogListPane(
                                uiState = uiState.value,
                            )
                        }
                    }
                }
            }
            mainClock.advanceTimeBy(NEW_LOGS_BUTTON_ANIMATION_DURATION_MILLIS)
            waitForIdle()

            onNodeWithTag(TELEMETRY_LOG_LIST_TEST_TAG).performScrollToIndex(SCROLL_TARGET_INDEX)
            mainClock.advanceTimeBy(NEW_LOGS_BUTTON_ANIMATION_DURATION_MILLIS)
            waitForIdle()

            uiState.value =
                uiState.value.copy(
                    logs =
                        listOf(
                            manyTelemetryLogs.first().copy(
                                id = NEW_LOG_ID,
                                createdAt = manyTelemetryLogs.first().createdAt + 1_000L,
                            ),
                        ) + manyTelemetryLogs,
                )
            waitForIdle()
            mainClock.advanceTimeBy(NEW_LOGS_BUTTON_ANIMATION_DURATION_MILLIS)
            waitForIdle()

            onRoot().captureRoboImage()
        }
}

private const val NEW_LOGS_BUTTON_ANIMATION_DURATION_MILLIS = 500L
private const val NEW_LOG_ID = 100L
private const val SCROLL_TARGET_INDEX = 15

private val manyTelemetryLogs =
    (30 downTo 1).map { index ->
        TelemetryLog(
            id = index.toLong(),
            createdAt = 1_800_000L + index * 1_000L,
            simulator = Simulator.LmuWindows,
            readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
            telemetryJson = """{"flag":"green","sector1":"clear","sector2":"clear","sector3":"clear"}""",
        )
    }
