package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsBrakeWearData
import kurou.kodriver.domain.repository.AceWindowsBrakeWearRepository

class ObserveAceWindowsBrakeWearUseCase(
    private val repository: AceWindowsBrakeWearRepository,
) {
    operator fun invoke(): Flow<AceWindowsBrakeWearData> = repository.brakeWearStream()
}
