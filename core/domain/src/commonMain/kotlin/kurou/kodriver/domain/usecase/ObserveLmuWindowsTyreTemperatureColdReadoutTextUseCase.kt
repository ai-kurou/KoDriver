package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository

class ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase(
    private val repository: LmuWindowsTyreTemperatureReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeColdReadoutText()
}
