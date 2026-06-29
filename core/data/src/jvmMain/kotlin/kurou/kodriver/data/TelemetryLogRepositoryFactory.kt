package kurou.kodriver.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kurou.kodriver.data.datasource.TelemetryLogDatabase
import kurou.kodriver.data.datasource.TelemetryLogDatabaseConstructor
import kurou.kodriver.data.repository.TelemetryLogRepositoryImpl
import kurou.kodriver.domain.repository.TelemetryLogRepository
import java.io.File

fun createTelemetryLogRepository(directory: String): TelemetryLogRepository {
    File(directory).mkdirs()
    val database = Room.databaseBuilder<TelemetryLogDatabase>(
        name = File(directory, "telemetry_logs.db").absolutePath,
        factory = { TelemetryLogDatabaseConstructor.initialize() },
    )
        .setDriver(BundledSQLiteDriver())
        .build()
    return TelemetryLogRepositoryImpl(database.telemetryLogDao())
}
