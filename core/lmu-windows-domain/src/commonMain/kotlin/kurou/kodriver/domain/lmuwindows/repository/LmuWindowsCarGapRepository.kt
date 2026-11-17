package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData

public interface LmuWindowsCarGapRepository {
    public fun carGapStream(): Flow<LmuWindowsCarGapData>
}
