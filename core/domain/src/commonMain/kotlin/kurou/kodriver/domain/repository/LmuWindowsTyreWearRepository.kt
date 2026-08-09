package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsTyreWearData

interface LmuWindowsTyreWearRepository {
    fun tyreWearStream(): Flow<LmuWindowsTyreWearData>
}
