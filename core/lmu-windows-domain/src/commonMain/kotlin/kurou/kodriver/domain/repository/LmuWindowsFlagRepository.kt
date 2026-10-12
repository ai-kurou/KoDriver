package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData

interface LmuWindowsFlagRepository {
    fun flagStream(): Flow<LmuWindowsRaceFlagsData>
}
