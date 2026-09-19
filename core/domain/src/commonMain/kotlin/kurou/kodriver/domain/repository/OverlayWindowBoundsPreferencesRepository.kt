package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverlayWindowBounds

interface OverlayWindowBoundsPreferencesRepository {
    fun observeOverlayWindowBounds(): Flow<OverlayWindowBounds>

    suspend fun saveOverlayWindowBounds(bounds: OverlayWindowBounds)
}
