package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData
import kurou.kodriver.domain.acewindows.repository.AceWindowsOverallPositionRepository

class ObserveAceWindowsOverallPositionUseCase(
    private val repository: AceWindowsOverallPositionRepository,
) {
    operator fun invoke(): Flow<AceWindowsOverallPositionData> = repository.overallPositionStream()
}
