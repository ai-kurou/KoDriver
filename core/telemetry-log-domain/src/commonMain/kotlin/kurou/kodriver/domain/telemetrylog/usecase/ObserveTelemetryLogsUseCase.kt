package kurou.kodriver.domain.telemetrylog.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.telemetrylog.model.TelemetryLog
import kurou.kodriver.domain.telemetrylog.repository.TelemetryLogRepository

public class ObserveTelemetryLogsUseCase(
    private val repository: TelemetryLogRepository,
) {
    public operator fun invoke(): Flow<List<TelemetryLog>> = repository.observeTelemetryLogs()
}
