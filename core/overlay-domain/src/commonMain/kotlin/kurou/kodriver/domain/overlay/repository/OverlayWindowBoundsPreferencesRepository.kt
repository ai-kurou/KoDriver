package kurou.kodriver.domain.overlay.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.overlay.model.OverlayWindowBounds

public interface OverlayWindowBoundsPreferencesRepository {
    public fun observeOverlayWindowBounds(): Flow<OverlayWindowBounds>

    public suspend fun saveOverlayWindowBounds(bounds: OverlayWindowBounds)
}
