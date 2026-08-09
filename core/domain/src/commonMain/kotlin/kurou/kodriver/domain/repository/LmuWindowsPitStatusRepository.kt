package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsPitStatusData

interface LmuWindowsPitStatusRepository {
    fun pitStatusStream(): Flow<LmuWindowsPitStatusData>
}
