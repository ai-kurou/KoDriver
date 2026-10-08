package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository

class ObserveAceWindowsMyBestLapReadoutTextUseCase(
    private val repository: AceWindowsMyBestLapPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
