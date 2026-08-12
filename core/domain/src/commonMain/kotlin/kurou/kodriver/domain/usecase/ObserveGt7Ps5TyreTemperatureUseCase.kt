package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.repository.Gt7Ps5Repository

class ObserveGt7Ps5TyreTemperatureUseCase(
    private val repository: Gt7Ps5Repository,
) {
    operator fun invoke(): Flow<Gt7Ps5TyreTemperatureData> = repository.telemetryStream().map { it.tyreTemperature }
}
