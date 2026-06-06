package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.repository.ProximityRepository

internal class EmptyProximityRepository : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = emptyFlow()
}
