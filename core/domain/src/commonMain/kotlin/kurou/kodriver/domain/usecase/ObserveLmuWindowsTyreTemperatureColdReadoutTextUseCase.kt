package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

class ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase(
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeColdReadoutText()
}
