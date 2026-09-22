package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsRepository

class ObserveAceWindowsRemainingFuelLapsUseCase(
    private val repository: AceWindowsRemainingFuelLapsRepository,
) {
    operator fun invoke(): Flow<AceWindowsRemainingFuelLapsData> = repository.remainingFuelLapsStream()
}
