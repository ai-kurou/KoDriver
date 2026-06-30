package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail

interface TelemetryLogRepository {
    fun observeTelemetryLogs(): Flow<List<TelemetryLog>>

    fun observeTelemetryLogDetail(id: Long): Flow<TelemetryLogDetail?>

    suspend fun saveTelemetryLog(log: TelemetryLog)
}
