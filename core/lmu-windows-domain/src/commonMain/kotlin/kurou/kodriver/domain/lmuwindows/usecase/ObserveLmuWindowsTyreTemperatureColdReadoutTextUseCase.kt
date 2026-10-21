package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository

class ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase(
    private val repository: LmuWindowsTyreTemperatureReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeColdReadoutText()
}
