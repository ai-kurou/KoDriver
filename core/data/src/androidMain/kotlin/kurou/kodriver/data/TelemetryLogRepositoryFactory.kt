package kurou.kodriver.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kurou.kodriver.data.datasource.TelemetryLogDatabase
import kurou.kodriver.data.datasource.TelemetryLogDatabaseConstructor
import kurou.kodriver.data.repository.TelemetryLogRepositoryImpl
import kurou.kodriver.domain.repository.TelemetryLogRepository

fun createTelemetryLogRepository(context: Context): TelemetryLogRepository {
    val database = Room.databaseBuilder<TelemetryLogDatabase>(
        context = context,
        name = "telemetry_logs.db",
        factory = { TelemetryLogDatabaseConstructor.initialize() },
    )
        .setDriver(BundledSQLiteDriver())
        .build()
    return TelemetryLogRepositoryImpl(database.telemetryLogDao())
}
