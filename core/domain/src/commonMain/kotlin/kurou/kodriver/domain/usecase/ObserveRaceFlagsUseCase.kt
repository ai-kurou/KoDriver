package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.repository.FlagRepository

class ObserveRaceFlagsUseCase(private val repository: FlagRepository) {
    operator fun invoke(): Flow<RaceFlagsData> = repository.flagStream()
}
