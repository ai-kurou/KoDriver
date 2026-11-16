package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

public class SaveGt7Ps5TyreTemperatureHighThresholdUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    public suspend operator fun invoke(celsius: Celsius): Unit = repository.saveHighThresholdCelsius(celsius)
}
