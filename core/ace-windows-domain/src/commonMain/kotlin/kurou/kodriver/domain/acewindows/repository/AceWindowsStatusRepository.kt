package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusData

public interface AceWindowsStatusRepository {
    public fun statusStream(): Flow<AceWindowsStatusData>
}
