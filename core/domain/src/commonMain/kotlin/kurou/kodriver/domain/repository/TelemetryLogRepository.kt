package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TelemetryLog

interface TelemetryLogRepository {
    fun observeTelemetryLogs(): Flow<List<TelemetryLog>>

    suspend fun saveTelemetryLog(log: TelemetryLog)
}
