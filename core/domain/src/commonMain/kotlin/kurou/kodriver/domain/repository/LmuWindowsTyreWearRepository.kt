package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsTyreWearData

interface LmuWindowsTyreWearRepository {
    fun tyreWearStream(): Flow<LmuWindowsTyreWearData>
}
