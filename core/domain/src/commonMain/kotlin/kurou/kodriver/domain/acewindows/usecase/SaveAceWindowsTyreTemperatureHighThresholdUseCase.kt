package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

class SaveAceWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(celsius: Celsius) = repository.saveHighThresholdCelsius(celsius)
}
