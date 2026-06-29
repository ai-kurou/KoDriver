package kurou.kodriver.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.data.model.TelemetryLogEntity

internal class InMemoryTelemetryLogDao : TelemetryLogDao {
    private val logs = MutableStateFlow(emptyList<TelemetryLogEntity>())
    private var nextId = 1L

    override fun observeTelemetryLogs(): Flow<List<TelemetryLogEntity>> = logs

    override suspend fun insert(log: TelemetryLogEntity) {
        val savedLog = log.copy(id = log.id.takeIf { it != 0L } ?: nextId++)
        logs.update { currentLogs -> listOf(savedLog) + currentLogs }
    }
}
