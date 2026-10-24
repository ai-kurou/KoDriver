package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.DismissedVolumes

interface LowVolumeWarningDismissalRepository {
    fun observeDismissedVolumes(): Flow<DismissedVolumes>

    suspend fun saveDismissedVolumes(dismissedVolumes: DismissedVolumes)
}
