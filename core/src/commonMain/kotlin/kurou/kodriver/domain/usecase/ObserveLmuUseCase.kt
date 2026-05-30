package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuTelemetryData
import kurou.kodriver.domain.repository.LmuRepository

class ObserveLmuUseCase(private val repository: LmuRepository) {
    operator fun invoke(): Flow<LmuTelemetryData> = repository.telemetryStream()
}
