package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository

class ObserveAceWindowsRemainingFuelReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
