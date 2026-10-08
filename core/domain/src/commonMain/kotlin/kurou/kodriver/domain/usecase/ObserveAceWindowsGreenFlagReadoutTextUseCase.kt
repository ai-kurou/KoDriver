package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository

class ObserveAceWindowsGreenFlagReadoutTextUseCase(
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeText(AceWindowsFlagReadoutTextKey.GREEN)
}
