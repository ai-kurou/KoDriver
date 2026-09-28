package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

class ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase(
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeSectorYellowFlagText()
}
