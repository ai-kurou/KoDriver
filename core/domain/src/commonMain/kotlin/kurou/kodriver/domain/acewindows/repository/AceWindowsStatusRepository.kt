package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusData

interface AceWindowsStatusRepository {
    fun statusStream(): Flow<AceWindowsStatusData>
}
