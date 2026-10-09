package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData

interface LmuWindowsBrakeWearRepository {
    fun brakeWearStream(): Flow<LmuWindowsBrakeWearData>
}
