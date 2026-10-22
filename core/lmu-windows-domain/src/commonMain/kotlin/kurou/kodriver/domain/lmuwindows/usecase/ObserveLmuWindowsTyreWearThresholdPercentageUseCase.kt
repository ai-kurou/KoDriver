package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository

class ObserveLmuWindowsTyreWearThresholdPercentageUseCase(
    private val repository: LmuWindowsTyreWearPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeThresholdPercentage()
}
