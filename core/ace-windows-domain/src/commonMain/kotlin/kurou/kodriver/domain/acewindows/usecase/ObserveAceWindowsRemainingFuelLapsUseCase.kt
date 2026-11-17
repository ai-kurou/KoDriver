package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsRepository

public class ObserveAceWindowsRemainingFuelLapsUseCase(
    private val repository: AceWindowsRemainingFuelLapsRepository,
) {
    public operator fun invoke(): Flow<AceWindowsRemainingFuelLapsData> = repository.remainingFuelLapsStream()
}
