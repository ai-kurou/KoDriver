package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository

class SaveAceWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(celsius: Celsius) = repository.saveHighThresholdCelsius(celsius)
}
