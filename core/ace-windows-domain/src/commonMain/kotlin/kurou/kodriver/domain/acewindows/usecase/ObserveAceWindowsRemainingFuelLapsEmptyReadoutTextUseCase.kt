package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository

public class ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeEmptyReadoutText()
}
