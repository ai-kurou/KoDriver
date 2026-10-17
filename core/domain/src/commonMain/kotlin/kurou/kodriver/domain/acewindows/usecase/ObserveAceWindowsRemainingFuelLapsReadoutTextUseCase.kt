package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository

class ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
