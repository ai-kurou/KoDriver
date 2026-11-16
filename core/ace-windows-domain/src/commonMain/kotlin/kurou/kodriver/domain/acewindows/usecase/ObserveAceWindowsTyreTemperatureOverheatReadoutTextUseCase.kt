package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository

public class ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeOverheatReadoutText()
}
