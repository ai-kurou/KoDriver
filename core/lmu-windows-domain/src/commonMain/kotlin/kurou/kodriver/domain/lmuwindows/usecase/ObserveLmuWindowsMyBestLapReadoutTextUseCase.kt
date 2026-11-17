package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsMyBestLapPreferencesRepository

public class ObserveLmuWindowsMyBestLapReadoutTextUseCase(
    private val repository: LmuWindowsMyBestLapPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
