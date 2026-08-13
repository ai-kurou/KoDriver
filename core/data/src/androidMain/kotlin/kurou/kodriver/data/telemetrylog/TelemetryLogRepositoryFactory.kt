package kurou.kodriver.data.telemetrylog

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kurou.kodriver.domain.repository.TelemetryLogRepository

/**
 * TelemetryLog Repository の永続化実装を生成する。
 */
fun createTelemetryLogRepository(context: Context): TelemetryLogRepository {
    val database =
        Room
            .databaseBuilder<TelemetryLogDatabase>(
                context = context,
                name = "telemetry_logs.db",
                factory = { TelemetryLogDatabaseConstructor.initialize() },
            ).setDriver(BundledSQLiteDriver())
            .build()
    return TelemetryLogRepositoryImpl(database.telemetryLogDao())
}
