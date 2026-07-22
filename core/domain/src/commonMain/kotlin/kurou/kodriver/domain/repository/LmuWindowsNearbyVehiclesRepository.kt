package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsNearbyVehiclesData

interface LmuWindowsNearbyVehiclesRepository {
    fun nearbyVehiclesStream(): Flow<LmuWindowsNearbyVehiclesData>
}
