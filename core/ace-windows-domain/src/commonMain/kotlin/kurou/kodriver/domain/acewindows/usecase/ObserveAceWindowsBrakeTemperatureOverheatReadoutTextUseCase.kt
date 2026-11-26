package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperaturePreferencesRepository

public class ObserveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase(
    private val repository: AceWindowsBrakeTemperaturePreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeOverheatReadoutText()
}
