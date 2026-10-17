package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

class ObserveAceWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Celsius> = repository.observeHighThresholdCelsius()
}
