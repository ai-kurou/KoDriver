package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsNearbyVehiclesData
import kurou.kodriver.domain.repository.LmuWindowsNearbyVehiclesRepository

class ObserveLmuWindowsNearbyVehiclesUseCase(private val repository: LmuWindowsNearbyVehiclesRepository) {
    operator fun invoke(): Flow<LmuWindowsNearbyVehiclesData> = repository.nearbyVehiclesStream()
}
