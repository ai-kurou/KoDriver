package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.repository.FlagRepository

internal class EmptyFlagRepository : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = emptyFlow()
}
