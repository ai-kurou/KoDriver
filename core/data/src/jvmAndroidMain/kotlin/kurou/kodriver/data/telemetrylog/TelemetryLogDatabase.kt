package kurou.kodriver.data.telemetrylog

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [TelemetryLogEntity::class],
    version = 1,
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
