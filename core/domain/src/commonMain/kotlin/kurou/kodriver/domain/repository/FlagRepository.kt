package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.RaceFlagsData

interface FlagRepository {
    fun flagStream(): Flow<RaceFlagsData>
}
