package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository

class ObserveLmuWindowsBrakeWearUseCase(
    private val repository: LmuWindowsBrakeWearRepository,
) {
    operator fun invoke(): Flow<LmuWindowsBrakeWearData?> = repository.brakeWearStream()
}
