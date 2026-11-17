package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData

public interface LmuWindowsVirtualEnergyRepository {
    public fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData>
}
