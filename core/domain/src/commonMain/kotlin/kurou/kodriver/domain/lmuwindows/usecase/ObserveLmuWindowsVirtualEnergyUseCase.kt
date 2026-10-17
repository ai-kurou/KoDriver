package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVirtualEnergyRepository

class ObserveLmuWindowsVirtualEnergyUseCase(
    private val repository: LmuWindowsVirtualEnergyRepository,
) {
    operator fun invoke(): Flow<LmuWindowsVirtualEnergyData> = repository.virtualEnergyStream()
}
