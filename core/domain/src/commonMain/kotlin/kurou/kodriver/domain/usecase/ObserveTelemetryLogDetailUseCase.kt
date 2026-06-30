package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository

class ObserveTelemetryLogDetailUseCase(
    private val repository: TelemetryLogRepository,
) {
    operator fun invoke(id: Long): Flow<TelemetryLogDetail?> = repository.observeTelemetryLogDetail(id)
}
