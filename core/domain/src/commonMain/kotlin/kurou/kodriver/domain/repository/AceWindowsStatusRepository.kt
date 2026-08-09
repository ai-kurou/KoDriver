package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.AceWindowsStatusData

interface AceWindowsStatusRepository {
    fun statusStream(): Flow<AceWindowsStatusData>
}
