package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.AceWindowsFlagData

interface AceWindowsFlagRepository {
    fun flagStream(): Flow<AceWindowsFlagData>
}
