package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository

class ObserveTelemetryLogsUseCase(
    private val repository: TelemetryLogRepository,
) {
    operator fun invoke(): Flow<List<TelemetryLog>> = repository.observeTelemetryLogs()
}
