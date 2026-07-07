package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsProximityData

interface LmuWindowsProximityRepository {
    fun proximityStream(): Flow<LmuWindowsProximityData>
}
