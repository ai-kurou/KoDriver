package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData

public interface AceWindowsFlagRepository {
    public fun flagStream(): Flow<AceWindowsFlagData>
}
