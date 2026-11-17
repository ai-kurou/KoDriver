package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitStatusData

public interface LmuWindowsPitStatusRepository {
    public fun pitStatusStream(): Flow<LmuWindowsPitStatusData>
}
