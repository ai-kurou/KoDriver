package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository

public class ObserveAceWindowsRemainingFuelReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
