package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TelemetryData
import kurou.kodriver.domain.repository.TelemetryRepository

class ObserveTelemetryUseCase(private val repository: TelemetryRepository) {
    operator fun invoke(): Flow<TelemetryData> = repository.telemetryStream()
}
