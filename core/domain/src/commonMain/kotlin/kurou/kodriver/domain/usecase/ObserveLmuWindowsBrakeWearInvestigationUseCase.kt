package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearInvestigationRepository

class ObserveLmuWindowsBrakeWearInvestigationUseCase(
    private val repository: LmuWindowsBrakeWearInvestigationRepository,
) {
    operator fun invoke(): Flow<LmuWindowsBrakeWearInvestigationData> = repository.investigationStream()
}
