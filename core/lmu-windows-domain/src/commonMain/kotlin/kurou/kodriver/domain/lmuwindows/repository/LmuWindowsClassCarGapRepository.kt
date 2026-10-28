package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData

interface LmuWindowsClassCarGapRepository {
    fun classCarGapStream(): Flow<LmuWindowsClassCarGapData>
}
