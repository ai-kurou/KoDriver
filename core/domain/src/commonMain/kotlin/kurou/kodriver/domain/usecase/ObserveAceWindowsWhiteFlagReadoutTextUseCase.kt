package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository

class ObserveAceWindowsWhiteFlagReadoutTextUseCase(
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeWhiteFlagText()
}
