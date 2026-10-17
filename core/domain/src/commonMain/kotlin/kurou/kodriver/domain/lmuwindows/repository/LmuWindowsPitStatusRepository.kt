package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitStatusData

interface LmuWindowsPitStatusRepository {
    fun pitStatusStream(): Flow<LmuWindowsPitStatusData>
}
