package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData

interface LmuWindowsBrakeWearInvestigationRepository {
    fun investigationStream(): Flow<LmuWindowsBrakeWearInvestigationData>
}
