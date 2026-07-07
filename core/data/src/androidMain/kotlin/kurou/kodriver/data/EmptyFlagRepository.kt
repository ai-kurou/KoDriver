package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository

internal class EmptyFlagRepository : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = emptyFlow()
}
