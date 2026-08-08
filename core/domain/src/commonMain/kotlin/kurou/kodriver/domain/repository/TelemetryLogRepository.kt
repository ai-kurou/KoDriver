package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail

interface TelemetryLogRepository {
    fun observeTelemetryLogs(): Flow<List<TelemetryLog>>

    fun observeTelemetryLogDetail(id: Long): Flow<TelemetryLogDetail?>

    suspend fun saveTelemetryLog(
        createdAt: Long,
        simulator: Simulator,
        readoutItemKey: ReadoutItemKey,
        telemetryJson: String,
    )

    suspend fun deleteAllTelemetryLogs()

    suspend fun deleteTelemetryLog(id: Long)
}
