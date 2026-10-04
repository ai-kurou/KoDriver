package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository

class ObserveLmuWindowsMyBestLapReadoutTextUseCase(
    private val repository: LmuWindowsMyBestLapPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
