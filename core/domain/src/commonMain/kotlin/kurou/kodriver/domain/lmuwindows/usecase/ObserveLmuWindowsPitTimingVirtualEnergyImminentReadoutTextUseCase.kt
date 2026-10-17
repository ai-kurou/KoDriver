package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository

class ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase(
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeVirtualEnergyImminentReadoutText()
}
