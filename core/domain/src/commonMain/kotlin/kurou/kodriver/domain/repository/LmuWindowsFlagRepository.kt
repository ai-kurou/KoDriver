package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsRaceFlagsData

interface LmuWindowsFlagRepository {
    fun flagStream(): Flow<LmuWindowsRaceFlagsData>
}
