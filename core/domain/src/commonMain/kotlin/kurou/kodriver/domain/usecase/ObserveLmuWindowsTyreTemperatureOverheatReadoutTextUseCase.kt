package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository

class ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase(
    private val repository: LmuWindowsTyreTemperatureReadoutTextPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeOverheatReadoutText()
}
