package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository

internal class FakeTelemetryLogRepository(
    initialLogs: List<TelemetryLog> = emptyList(),
) : TelemetryLogRepository {
    private val logs = MutableStateFlow(initialLogs)

    override fun observeTelemetryLogs(): Flow<List<TelemetryLog>> = logs

    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        logs.value += log
    }
}
