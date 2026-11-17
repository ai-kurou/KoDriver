package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository

public class ObserveTelemetryLogsUseCase(
    private val repository: TelemetryLogRepository,
) {
    public operator fun invoke(): Flow<List<TelemetryLog>> = repository.observeTelemetryLogs()
}
