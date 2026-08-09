package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository

class ObserveLmuWindowsTyreWearUseCase(
    private val repository: LmuWindowsTyreWearRepository,
) {
    operator fun invoke(): Flow<LmuWindowsTyreWearData> = repository.tyreWearStream()
}
