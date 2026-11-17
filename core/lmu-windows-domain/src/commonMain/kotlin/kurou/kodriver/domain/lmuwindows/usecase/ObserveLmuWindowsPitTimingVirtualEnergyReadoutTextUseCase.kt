package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository

public class ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeVirtualEnergyReadoutText()
}
