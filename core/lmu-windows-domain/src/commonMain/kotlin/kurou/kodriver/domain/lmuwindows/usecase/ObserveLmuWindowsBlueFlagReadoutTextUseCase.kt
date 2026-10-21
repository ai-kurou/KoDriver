package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagReadoutTextPreferencesRepository

class ObserveLmuWindowsBlueFlagReadoutTextUseCase(
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeBlueFlagText()
}
