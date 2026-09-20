package kurou.kodriver.data.telemetrylog

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [TelemetryLogEntity::class],
    version = 4,
    exportSchema = false,
)
@ConstructedBy(TelemetryLogDatabaseConstructor::class)
internal abstract class TelemetryLogDatabase : RoomDatabase() {
    abstract fun telemetryLogDao(): TelemetryLogDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object TelemetryLogDatabaseConstructor : RoomDatabaseConstructor<TelemetryLogDatabase> {
    override fun initialize(): TelemetryLogDatabase
}

/**
 * narratedText 追加に伴うスキーマ変更。既存行の narratedText は当時記録していなかったため
 * 復元できず、空文字列で埋める。
 */
internal val TELEMETRY_LOG_MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE telemetry_logs ADD COLUMN narratedText TEXT NOT NULL DEFAULT ''")
        }
    }

/**
 * wasQueued 追加に伴うスキーマ変更。既存行は読み上げ時点のキュー状態を記録しておらず
 * 復元できないため、false（キューオフ扱い）で埋める。
 */
internal val TELEMETRY_LOG_MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE telemetry_logs ADD COLUMN wasQueued INTEGER NOT NULL DEFAULT 0")
        }
    }

/**
 * wasQueued（Boolean）を narrationOutcome（[kurou.kodriver.domain.model.NarrationOutcome] の id）へ
 * 置き換えるスキーマ変更。読み上げされなかった項目も記録するようになり、2値では表現できなくなったため。
 *
 * 既存行はすべて読み上げ済みのものしか記録していないため、wasQueued=1 を queued へ移す。0 の行は
 * 割り込み再生だったのか、単に何も再生していない状態で読み上げただけなのかを復元できないため、
 * どちらとも言い切らない spoken（通常再生）に寄せる。
 * SQLite はカラムの型変更を直接行えないので、新テーブルを作って入れ替える定石の手順を踏む。
 */
internal val TELEMETRY_LOG_MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE telemetry_logs_new (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    createdAt INTEGER NOT NULL,
                    simulatorId TEXT NOT NULL,
                    readoutItemKey TEXT NOT NULL,
                    narratedText TEXT NOT NULL,
                    narrationOutcome TEXT NOT NULL,
                    telemetryJson TEXT NOT NULL
                )
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO telemetry_logs_new (
                    id, createdAt, simulatorId, readoutItemKey, narratedText, narrationOutcome, telemetryJson
                )
                SELECT
                    id, createdAt, simulatorId, readoutItemKey, narratedText,
                    CASE WHEN wasQueued = 1 THEN 'queued' ELSE 'spoken' END,
                    telemetryJson
                FROM telemetry_logs
                """.trimIndent(),
            )
            connection.execSQL("DROP TABLE telemetry_logs")
            connection.execSQL("ALTER TABLE telemetry_logs_new RENAME TO telemetry_logs")
        }
    }
