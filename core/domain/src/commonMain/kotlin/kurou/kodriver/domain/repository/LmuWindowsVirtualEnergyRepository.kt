package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsVirtualEnergyData

interface LmuWindowsVirtualEnergyRepository {
    fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData>
}
