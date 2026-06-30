package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository

internal class FakeTelemetryLogRepository(
    initialLogs: List<TelemetryLog> = emptyList(),
) : TelemetryLogRepository {
    private val logs = MutableStateFlow(initialLogs)

    override fun observeTelemetryLogs(): Flow<List<TelemetryLog>> = logs

    override fun observeTelemetryLogDetail(id: Long): Flow<TelemetryLogDetail?> =
        logs.map { logs ->
            val sortedLogs = logs.sortedWith(
                compareByDescending<TelemetryLog> { it.createdAt }.thenByDescending { it.id },
            )
            val index = sortedLogs.indexOfFirst { it.id == id }
            if (index == -1) {
                null
            } else {
                TelemetryLogDetail(
                    current = sortedLogs[index],
                    previous = sortedLogs.getOrNull(index + 1),
                )
            }
        }

    override suspend fun saveTelemetryLog(log: TelemetryLog) {
        logs.update { it + log }
    }
}
