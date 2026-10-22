package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository

class ObserveAceWindowsGreenFlagReadoutTextUseCase(
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeText(AceWindowsFlagReadoutTextKey.GREEN)
}
