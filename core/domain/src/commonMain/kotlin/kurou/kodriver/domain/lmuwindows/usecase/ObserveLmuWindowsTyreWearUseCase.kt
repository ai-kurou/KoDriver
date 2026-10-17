package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearRepository

class ObserveLmuWindowsTyreWearUseCase(
    private val repository: LmuWindowsTyreWearRepository,
) {
    operator fun invoke(): Flow<LmuWindowsTyreWearData> = repository.tyreWearStream()
}
