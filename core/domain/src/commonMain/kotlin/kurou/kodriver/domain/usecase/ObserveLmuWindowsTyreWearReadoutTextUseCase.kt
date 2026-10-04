package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository

class ObserveLmuWindowsTyreWearReadoutTextUseCase(
    private val repository: LmuWindowsTyreWearPreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeReadoutText()
}
