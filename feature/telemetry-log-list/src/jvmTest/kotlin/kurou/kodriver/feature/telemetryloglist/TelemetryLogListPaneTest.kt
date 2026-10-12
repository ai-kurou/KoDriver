package kurou.kodriver.feature.telemetryloglist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
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
