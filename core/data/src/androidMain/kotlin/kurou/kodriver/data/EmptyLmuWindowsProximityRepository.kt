package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.LmuWindowsProximityData
import kurou.kodriver.domain.repository.LmuWindowsProximityRepository

internal class EmptyLmuWindowsProximityRepository : LmuWindowsProximityRepository {
    override fun proximityStream(): Flow<LmuWindowsProximityData> = emptyFlow()
}
