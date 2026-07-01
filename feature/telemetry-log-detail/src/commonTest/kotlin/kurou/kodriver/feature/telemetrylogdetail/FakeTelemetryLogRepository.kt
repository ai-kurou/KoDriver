package kurou.kodriver.feature.telemetrylogdetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository

internal class FakeTelemetryLogRepository : TelemetryLogRepository {
    private val logs = MutableStateFlow(emptyList<TelemetryLog>())

    override fun observeTelemetryLogs() = logs

    override fun observeTelemetryLogDetail(id: Long) = logs.map { logs ->
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
        emit(logs.value + log)
    }

    fun emit(value: List<TelemetryLog>) {
        logs.update { value }
    }
}
