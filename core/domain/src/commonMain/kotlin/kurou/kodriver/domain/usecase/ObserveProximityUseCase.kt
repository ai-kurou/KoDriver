package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.repository.ProximityRepository

class ObserveProximityUseCase(private val repository: ProximityRepository) {
    operator fun invoke(): Flow<ProximityData> = repository.proximityStream()
}
