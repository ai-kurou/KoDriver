package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius

public class ObserveAceWindowsTyreTemperatureHighThresholdUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    public operator fun invoke(): Flow<Celsius> = repository.observeHighThresholdCelsius()
}
