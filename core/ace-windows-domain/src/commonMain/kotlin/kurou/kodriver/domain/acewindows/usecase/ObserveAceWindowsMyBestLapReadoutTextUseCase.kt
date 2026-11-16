package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository

public class ObserveAceWindowsMyBestLapReadoutTextUseCase(
    private val repository: AceWindowsMyBestLapPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
