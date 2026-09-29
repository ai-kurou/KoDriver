package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

class ObserveLmuWindowsBlackFlagReadoutTextUseCase(
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeBlackFlagText()
}
