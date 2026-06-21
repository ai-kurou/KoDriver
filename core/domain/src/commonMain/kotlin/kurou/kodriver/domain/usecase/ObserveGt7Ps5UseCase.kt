package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.repository.Gt7Ps5Repository

class ObserveGt7Ps5UseCase(private val repository: Gt7Ps5Repository) {
    operator fun invoke(): Flow<Gt7Ps5TelemetryData> = repository.telemetryStream()
}
