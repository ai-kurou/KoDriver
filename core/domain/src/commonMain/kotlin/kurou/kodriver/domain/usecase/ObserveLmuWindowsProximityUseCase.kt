package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsProximityData
import kurou.kodriver.domain.repository.LmuWindowsProximityRepository

class ObserveLmuWindowsProximityUseCase(private val repository: LmuWindowsProximityRepository) {
    operator fun invoke(): Flow<LmuWindowsProximityData> = repository.proximityStream()
}
