package kurou.kodriver.data.telemetrylog

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kurou.kodriver.domain.model.NarrationOutcome
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `バージョン2から3への移行で既存行にwasQueuedがfalseで追加される`() {
        val path = Files.createTempFile("telemetry_log_migration_test", ".db").toString()
        val connection = BundledSQLiteDriver().open(path)
        try {
            connection.execSQL(
                "CREATE TABLE telemetry_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "createdAt INTEGER NOT NULL, " +
                    "simulatorId TEXT NOT NULL, " +
                    "readoutItemKey TEXT NOT NULL, " +
                    "narratedText TEXT NOT NULL, " +
                    "telemetryJson TEXT NOT NULL)",
            )
            connection.execSQL(
                "INSERT INTO telemetry_logs (createdAt, simulatorId, readoutItemKey, narratedText, telemetryJson) " +
                    "VALUES (1000, 'lmu_windows', 'flag', 'イエローフラッグ', '{}')",
            )

            TELEMETRY_LOG_MIGRATION_2_3.migrate(connection)

            val statement = connection.prepare("SELECT createdAt, telemetryJson, wasQueued FROM telemetry_logs")
            try {
                assertTrue(statement.step())
                assertEquals(1000L, statement.getLong(0))
                assertEquals("{}", statement.getText(1))
                assertEquals(0L, statement.getLong(2))
            } finally {
                statement.close()
            }
        } finally {
            connection.close()
        }
    }

    @Test
    fun `バージョン3から4への移行でwasQueuedがnarrationOutcomeへ変換される`() {
        val path = Files.createTempFile("telemetry_log_migration_test", ".db").toString()
        val connection = BundledSQLiteDriver().open(path)
        try {
            connection.execSQL(
                "CREATE TABLE telemetry_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "createdAt INTEGER NOT NULL, " +
                    "simulatorId TEXT NOT NULL, " +
                    "readoutItemKey TEXT NOT NULL, " +
                    "narratedText TEXT NOT NULL, " +
                    "wasQueued INTEGER NOT NULL, " +
                    "telemetryJson TEXT NOT NULL)",
            )
            connection.execSQL(
                "INSERT INTO telemetry_logs " +
                    "(createdAt, simulatorId, readoutItemKey, narratedText, wasQueued, telemetryJson) " +
                    "VALUES (1000, 'lmu_windows', 'flag', 'イエローフラッグ', 1, '{\"a\":1}')",
            )
            connection.execSQL(
                "INSERT INTO telemetry_logs " +
                    "(createdAt, simulatorId, readoutItemKey, narratedText, wasQueued, telemetryJson) " +
                    "VALUES (2000, 'gt7_ps5', 'my_best_lap', '自己ベストラップ更新', 0, '{\"b\":2}')",
            )

            TELEMETRY_LOG_MIGRATION_3_4.migrate(connection)

            val statement =
                connection.prepare(
                    "SELECT id, createdAt, simulatorId, readoutItemKey, narratedText, narrationOutcome, " +
                        "telemetryJson FROM telemetry_logs ORDER BY id",
                )
            try {
                assertTrue(statement.step())
                assertEquals(1L, statement.getLong(0))
                assertEquals(1000L, statement.getLong(1))
                assertEquals("lmu_windows", statement.getText(2))
                assertEquals("flag", statement.getText(3))
                assertEquals("イエローフラッグ", statement.getText(4))
                assertEquals(NarrationOutcome.QUEUED.id, statement.getText(5))
                assertEquals("{\"a\":1}", statement.getText(6))

                assertTrue(statement.step())
                assertEquals(2L, statement.getLong(0))
                assertEquals(2000L, statement.getLong(1))
                assertEquals(NarrationOutcome.SPOKEN.id, statement.getText(5))
                assertEquals("{\"b\":2}", statement.getText(6))

                assertFalse(statement.step())
            } finally {
                statement.close()
            }
        } finally {
            connection.close()
        }
    }
}
