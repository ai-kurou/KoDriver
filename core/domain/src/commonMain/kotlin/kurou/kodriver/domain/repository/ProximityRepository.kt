package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ProximityData

interface ProximityRepository {
    fun proximityStream(): Flow<ProximityData>
}
