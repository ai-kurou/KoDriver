package kurou.kodriver.data.telemetrylog

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kurou.kodriver.domain.repository.TelemetryLogRepository
import java.io.File

/**
 * TelemetryLog Repository の永続化実装を生成する。
 */
fun createTelemetryLogRepository(directory: String): TelemetryLogRepository {
    File(directory).mkdirs()
    val database =
        Room
            .databaseBuilder<TelemetryLogDatabase>(
                name = File(directory, "telemetry_logs.db").absolutePath,
                factory = { TelemetryLogDatabaseConstructor.initialize() },
            ).setDriver(BundledSQLiteDriver())
            .build()
    return TelemetryLogRepositoryImpl(database.telemetryLogDao())
}
