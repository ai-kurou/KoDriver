package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository

class ObserveAceWindowsBestLapTimeUseCase(
    private val repository: AceWindowsBestLapTimeRepository,
) {
    operator fun invoke(): Flow<AceWindowsBestLapTimeData> = repository.bestLapTimeStream()
}
