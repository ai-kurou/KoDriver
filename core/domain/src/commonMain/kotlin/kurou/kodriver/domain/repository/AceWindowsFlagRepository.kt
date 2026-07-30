package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsFlagData

interface AceWindowsFlagRepository {
    fun flagStream(): Flow<AceWindowsFlagData>
}
