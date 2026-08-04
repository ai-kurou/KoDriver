package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsPitStatusData

interface LmuWindowsPitStatusRepository {
    fun pitStatusStream(): Flow<LmuWindowsPitStatusData>
}
