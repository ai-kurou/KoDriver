package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository

public class ObserveAceWindowsVehicleApproachReadoutTextUseCase(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
