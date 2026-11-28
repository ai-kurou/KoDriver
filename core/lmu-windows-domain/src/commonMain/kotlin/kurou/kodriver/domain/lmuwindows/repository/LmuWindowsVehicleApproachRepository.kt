package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsNearbyVehiclesData

public interface LmuWindowsVehicleApproachRepository {
    public fun nearbyVehiclesStream(): Flow<LmuWindowsNearbyVehiclesData>
}
