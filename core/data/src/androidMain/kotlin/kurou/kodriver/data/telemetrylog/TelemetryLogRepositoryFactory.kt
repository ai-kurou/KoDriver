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
            .addMigrations(TELEMETRY_LOG_MIGRATION_1_2)
            // マイグレーション未対応のテーブルのみを破棄する。true にすると
            // 将来他のテーブルを追加した際、無関係なテーブルまで巻き添えで
            // 削除されてしまうため false（デフォルト）を維持する。
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    return TelemetryLogRepositoryImpl(database.telemetryLogDao())
}
