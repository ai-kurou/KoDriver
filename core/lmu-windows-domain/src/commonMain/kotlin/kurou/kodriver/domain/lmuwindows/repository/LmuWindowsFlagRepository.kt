package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData

public interface LmuWindowsFlagRepository {
    public fun flagStream(): Flow<LmuWindowsRaceFlagsData>
}
