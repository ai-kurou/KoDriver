package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.repository.AceWindowsBestLapTimeRepository

class ObserveAceWindowsBestLapTimeUseCase(
    private val repository: AceWindowsBestLapTimeRepository,
) {
    operator fun invoke(): Flow<AceWindowsBestLapTimeData> = repository.bestLapTimeStream()
}
