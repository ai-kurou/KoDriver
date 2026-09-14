package kurou.kodriver.data.telemetrylog

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TelemetryLogRepositoryFactoryTest {
    @Test
    fun `Roomに保存したテレメトリログを観測できる`() =
        runTest {
            val directory = Files.createTempDirectory("kodriver_telemetry_log_test").toFile()
            val repository = createTelemetryLogRepository(directory.absolutePath)

            repository.saveTelemetryLog(
                createdAt = 123L,
                simulator = Simulator.Gt7Ps5,
                readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                narratedText = "自己ベストラップ更新",
                telemetryJson = """{"current":{}}""",
            )

            assertEquals(
                listOf(
                    TelemetryLog(
                        id = 1L,
                        createdAt = 123L,
                        simulator = Simulator.Gt7Ps5,
                        readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                        narratedText = "自己ベストラップ更新",
                        telemetryJson = """{"current":{}}""",
                    ),
                ),
                repository.observeTelemetryLogs().first(),
            )
            assertTrue(directory.resolve("telemetry_logs.db").exists())
        }

    @Test
    fun `バージョン1のDBファイルを開くとマイグレーションで既存ログを保持する`() =
        runTest {
            val directory = Files.createTempDirectory("kodriver_telemetry_log_migration_test").toFile()
            val dbFile = directory.resolve("telemetry_logs.db")
            val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
            try {
                connection.execSQL(
                    "CREATE TABLE telemetry_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "createdAt INTEGER NOT NULL, " +
                        "simulatorId TEXT NOT NULL, " +
                        "readoutItemKey TEXT NOT NULL, " +
                        "telemetryJson TEXT NOT NULL)",
                )
                connection.execSQL(
                    "INSERT INTO telemetry_logs (createdAt, simulatorId, readoutItemKey, telemetryJson) " +
                        "VALUES (2000, 'lmu_windows', 'lmu_windows_flag', '{}')",
                )
                connection.execSQL("PRAGMA user_version = 1")
            } finally {
                connection.close()
            }

            val repository = createTelemetryLogRepository(directory.absolutePath)

            assertEquals(
                listOf(
                    TelemetryLog(
                        id = 1L,
                        createdAt = 2000L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                        narratedText = "",
                        telemetryJson = "{}",
                    ),
                ),
                repository.observeTelemetryLogs().first(),
            )
        }
}
