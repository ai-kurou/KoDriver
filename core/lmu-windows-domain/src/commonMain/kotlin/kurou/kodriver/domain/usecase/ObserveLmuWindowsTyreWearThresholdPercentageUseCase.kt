package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository

class ObserveLmuWindowsTyreWearThresholdPercentageUseCase(
    private val repository: LmuWindowsTyreWearPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeThresholdPercentage()
}
