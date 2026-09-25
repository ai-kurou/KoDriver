package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperaturePreferencesRepository

class SaveLmuWindowsBrakeTemperatureHighThresholdUseCase(
    private val repository: LmuWindowsBrakeTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(celsius: Int) = repository.saveHighThresholdCelsius(celsius)
}
