package kurou.kodriver.domain.gt7ps5.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5Repository

class ObserveGt7Ps5UseCase(
    private val repository: Gt7Ps5Repository,
) {
    operator fun invoke(): Flow<Gt7Ps5TelemetryData> = repository.telemetryStream()
}
