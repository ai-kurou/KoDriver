package kurou.kodriver.data.telemetrylog

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TelemetryLogMigrationTest {
    @Test
    fun `バージョン1から2への移行で既存行にnarratedTextが空文字で追加される`() {
        val path = Files.createTempFile("telemetry_log_migration_test", ".db").toString()
        val connection = BundledSQLiteDriver().open(path)
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
                    "VALUES (1000, 'lmu_windows', 'flag', '{}')",
            )

            TELEMETRY_LOG_MIGRATION_1_2.migrate(connection)

            val statement = connection.prepare("SELECT createdAt, telemetryJson, narratedText FROM telemetry_logs")
            try {
                assertTrue(statement.step())
                assertEquals(1000L, statement.getLong(0))
                assertEquals("{}", statement.getText(1))
                assertEquals("", statement.getText(2))
            } finally {
                statement.close()
            }
        } finally {
            connection.close()
        }
    }
}
