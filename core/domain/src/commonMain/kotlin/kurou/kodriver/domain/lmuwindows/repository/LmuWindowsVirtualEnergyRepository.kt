package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData

interface LmuWindowsVirtualEnergyRepository {
    fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData>
}
