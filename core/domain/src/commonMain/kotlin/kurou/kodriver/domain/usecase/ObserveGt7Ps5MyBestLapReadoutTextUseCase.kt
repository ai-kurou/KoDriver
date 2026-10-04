package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository

class ObserveGt7Ps5MyBestLapReadoutTextUseCase(
    private val repository: Gt7Ps5MyBestLapPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
