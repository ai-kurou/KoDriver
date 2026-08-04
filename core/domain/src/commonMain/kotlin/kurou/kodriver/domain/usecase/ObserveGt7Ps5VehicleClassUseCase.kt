package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.repository.Gt7Ps5Repository

class ObserveGt7Ps5VehicleClassUseCase(
    private val repository: Gt7Ps5Repository,
) {
    operator fun invoke(): Flow<String> = repository.telemetryStream().map { it.carCategory }
}
