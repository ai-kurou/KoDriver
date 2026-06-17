package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.repository.LmuWindowsRepository

class ObserveLmuWindowsUseCase(private val repository: LmuWindowsRepository) {
    operator fun invoke(): Flow<LmuWindowsTelemetryData> = repository.telemetryStream()
}
