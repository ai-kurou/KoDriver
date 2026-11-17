package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository

public class ObserveTelemetryLogDetailUseCase(
    private val repository: TelemetryLogRepository,
) {
    public operator fun invoke(id: Long): Flow<TelemetryLogDetail?> = repository.observeTelemetryLogDetail(id)
}
