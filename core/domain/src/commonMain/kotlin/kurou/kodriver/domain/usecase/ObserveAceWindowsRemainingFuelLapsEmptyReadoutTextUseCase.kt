package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository

class ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeEmptyReadoutText()
}
