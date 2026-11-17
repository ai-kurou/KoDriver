package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

public class ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase(
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
