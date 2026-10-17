package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsRepository

class ObserveAceWindowsRemainingFuelLapsUseCase(
    private val repository: AceWindowsRemainingFuelLapsRepository,
) {
    operator fun invoke(): Flow<AceWindowsRemainingFuelLapsData> = repository.remainingFuelLapsStream()
}
