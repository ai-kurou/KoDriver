package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverlayWindowBounds

public interface OverlayWindowBoundsPreferencesRepository {
    public fun observeOverlayWindowBounds(): Flow<OverlayWindowBounds>

    public suspend fun saveOverlayWindowBounds(bounds: OverlayWindowBounds)
}
