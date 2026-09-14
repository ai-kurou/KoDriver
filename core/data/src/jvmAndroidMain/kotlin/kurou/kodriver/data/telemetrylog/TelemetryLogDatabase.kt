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
    version = 2,
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
