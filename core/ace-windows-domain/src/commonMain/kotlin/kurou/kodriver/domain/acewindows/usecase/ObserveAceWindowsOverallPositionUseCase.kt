package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData
import kurou.kodriver.domain.acewindows.repository.AceWindowsOverallPositionRepository

public class ObserveAceWindowsOverallPositionUseCase(
    private val repository: AceWindowsOverallPositionRepository,
) {
    public operator fun invoke(): Flow<AceWindowsOverallPositionData> = repository.overallPositionStream()
}
