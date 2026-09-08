package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData

interface LmuWindowsTyreDetachedRepository {
    fun tyreDetachedStream(): Flow<LmuWindowsTyreDetachedData>
}
