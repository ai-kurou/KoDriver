package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository

class ObserveLmuWindowsRemainingFuelReadoutTextUseCase(
    private val repository: LmuWindowsRemainingFuelPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
