package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository

class ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeOverheatReadoutText()
}
