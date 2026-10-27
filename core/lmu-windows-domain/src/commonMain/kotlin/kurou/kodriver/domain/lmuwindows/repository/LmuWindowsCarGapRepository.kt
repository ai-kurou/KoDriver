package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData

interface LmuWindowsCarGapRepository {
    fun carGapStream(): Flow<LmuWindowsCarGapData>
}
