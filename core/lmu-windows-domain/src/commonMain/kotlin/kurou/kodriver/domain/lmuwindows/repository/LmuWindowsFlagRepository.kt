package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData

interface LmuWindowsFlagRepository {
    fun flagStream(): Flow<LmuWindowsRaceFlagsData>
}
