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
            .addMigrations(TELEMETRY_LOG_MIGRATION_1_2)
            // マイグレーション未対応のテーブルのみを破棄する。true にすると
            // 将来他のテーブルを追加した際、無関係なテーブルまで巻き添えで
            // 削除されてしまうため false（デフォルト）を維持する。
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    return TelemetryLogRepositoryImpl(database.telemetryLogDao())
}
