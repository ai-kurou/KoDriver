package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository

class ObserveLmuWindowsRaceFlagsUseCase(
    private val repository: LmuWindowsFlagRepository,
) {
    operator fun invoke(): Flow<LmuWindowsRaceFlagsData> = repository.flagStream()
}
