package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData

interface LmuWindowsTyreDetachedRepository {
    fun tyreDetachedStream(): Flow<LmuWindowsTyreDetachedData>
}
