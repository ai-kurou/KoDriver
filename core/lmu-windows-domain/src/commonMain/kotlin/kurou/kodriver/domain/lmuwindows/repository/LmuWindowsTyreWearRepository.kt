package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData

public interface LmuWindowsTyreWearRepository {
    public fun tyreWearStream(): Flow<LmuWindowsTyreWearData>
}
