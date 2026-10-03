package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

class ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase(
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
