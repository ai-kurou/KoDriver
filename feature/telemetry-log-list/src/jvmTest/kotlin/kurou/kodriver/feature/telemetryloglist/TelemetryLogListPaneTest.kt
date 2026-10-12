package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryLogListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `選択中のシミュレーターがない場合は既存の空状態を表示する`() {
        rule.setContent {
            KoDriverTheme {
                TelemetryLogListPane(uiState = TelemetryLogListUiState(selectedSimulator = null))
            }
        }

        rule.onNodeWithText("ログはまだありません").assertIsDisplayed()
        rule.onNodeWithText("テレメトリを受信すると、ここに新しい順で表示されます。").assertIsDisplayed()
    }

    @Test
    fun `選択中のシミュレーターの表示名を含む空状態を表示する`() {
        rule.setContent {
            KoDriverTheme {
                TelemetryLogListPane(uiState = TelemetryLogListUiState(selectedSimulator = Simulator.LmuWindows))
            }
        }

        rule.onNodeWithText("Le Mans Ultimate（Windows版）のログはまだありません").assertIsDisplayed()
        rule.onNodeWithText("テレメトリを受信すると、ここに新しい順で表示されます。").assertIsDisplayed()
    }

    @Test
    fun `シミュレーターを切り替えると新着ログボタンを非表示にしてリストを先頭へ戻す`() {
        val logs = (30 downTo 1).map { createTelemetryLog(id = it.toLong()) }
        val uiState =
            mutableStateOf(TelemetryLogListUiState(logs = logs, selectedSimulator = Simulator.LmuWindows))
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.height(240.dp)) {
                    TelemetryLogListPane(uiState = uiState.value)
                }
            }
        }

        rule.onNodeWithTag(TELEMETRY_LOG_LIST_TEST_TAG).performScrollToIndex(15)
        rule.runOnIdle {
            uiState.value = uiState.value.copy(logs = listOf(createTelemetryLog(id = 100)) + logs)
        }
        rule.onNodeWithText("新しいログ").assertIsDisplayed()

        rule.runOnIdle {
            uiState.value =
                TelemetryLogListUiState(
                    selectedSimulator = Simulator.AceWindows,
                    logs =
                        (60 downTo 31).map {
                            createTelemetryLog(
                                id = it.toLong(),
                                simulator = Simulator.AceWindows,
                                readoutItemKey = AceWindowsReadoutItemKey.Flag.Root,
                            )
                        },
                )
        }

        rule.onNodeWithText("新しいログ").assertDoesNotExist()
        rule.onNodeWithText("データベースをリセット").assertIsDisplayed()
        rule.onNodeWithText("09:00:00.060 / レース +00:00:00.029").assertIsDisplayed()
    }

    @Test
    fun `UTC0時0分0秒はJST9時0分0秒として表示する`() {
        val text = formatTelemetryLogTime(createdAt = 0L, raceElapsedMs = 0L)

        assertEquals("09:00:00.000 / レース +00:00:00.000", text)
    }

    @Test
    fun `日付をまたぐ時刻もJSTの時刻として表示する`() {
        val millisecondsPerDay = 24 * 60 * 60 * 1_000L
        val fifteenHoursInMillis = 15 * 60 * 60 * 1_000L

        val text = formatTelemetryLogTime(createdAt = fifteenHoursInMillis, raceElapsedMs = 0L)

        assertEquals("00:00:00.000 / レース +00:00:00.000", text)
        assertEquals(0L, (fifteenHoursInMillis + 9 * 60 * 60 * 1_000L) % millisecondsPerDay)
    }

    @Test
    fun `レース経過時間はcreatedAtとraceStartedAtの差分をそのまま表示する`() {
        val oneHourInMillis = 60 * 60 * 1_000L

        val text = formatTelemetryLogTime(createdAt = oneHourInMillis, raceElapsedMs = oneHourInMillis)

        assertEquals("10:00:00.000 / レース +01:00:00.000", text)
    }
}
