package kurou.kodriver.data.lowvolumewarning

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.DismissedVolumes
import kurou.kodriver.domain.repository.LowVolumeWarningDismissalRepository

class InMemoryLowVolumeWarningDismissalRepository : LowVolumeWarningDismissalRepository {
    private val dismissedVolumes = MutableStateFlow(DismissedVolumes())

    override fun observeDismissedVolumes(): Flow<DismissedVolumes> = dismissedVolumes.asStateFlow()

    override suspend fun saveDismissedVolumes(dismissedVolumes: DismissedVolumes) {
        this.dismissedVolumes.update { dismissedVolumes }
    }
}
