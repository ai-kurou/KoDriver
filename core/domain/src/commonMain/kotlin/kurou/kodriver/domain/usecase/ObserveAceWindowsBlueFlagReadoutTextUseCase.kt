package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository

class ObserveAceWindowsBlueFlagReadoutTextUseCase(
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeBlueFlagText()
}
