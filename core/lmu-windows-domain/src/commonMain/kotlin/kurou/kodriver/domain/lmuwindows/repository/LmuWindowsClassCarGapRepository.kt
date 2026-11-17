package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData

public interface LmuWindowsClassCarGapRepository {
    public fun classCarGapStream(): Flow<LmuWindowsClassCarGapData>
}
