package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData

interface AceWindowsFlagRepository {
    fun flagStream(): Flow<AceWindowsFlagData>
}
