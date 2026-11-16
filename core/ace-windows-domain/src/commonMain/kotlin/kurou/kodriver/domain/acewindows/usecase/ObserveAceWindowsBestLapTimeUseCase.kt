package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.repository.AceWindowsBestLapTimeRepository

public class ObserveAceWindowsBestLapTimeUseCase(
    private val repository: AceWindowsBestLapTimeRepository,
) {
    public operator fun invoke(): Flow<AceWindowsBestLapTimeData> = repository.bestLapTimeStream()
}
