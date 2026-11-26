package kurou.kodriver.domain.telemetrylog.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.telemetrylog.model.TelemetryLogDetail
import kurou.kodriver.domain.telemetrylog.repository.TelemetryLogRepository

public class ObserveTelemetryLogDetailUseCase(
    private val repository: TelemetryLogRepository,
) {
    public operator fun invoke(id: Long): Flow<TelemetryLogDetail?> = repository.observeTelemetryLogDetail(id)
}
