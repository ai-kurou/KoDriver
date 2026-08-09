package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository

class ObserveLmuWindowsVirtualEnergyUseCase(
    private val repository: LmuWindowsVirtualEnergyRepository,
) {
    operator fun invoke(): Flow<LmuWindowsVirtualEnergyData> = repository.virtualEnergyStream()
}
