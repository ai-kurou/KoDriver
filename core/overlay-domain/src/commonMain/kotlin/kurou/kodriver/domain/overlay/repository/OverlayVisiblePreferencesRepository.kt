package kurou.kodriver.domain.overlay.repository

import kotlinx.coroutines.flow.Flow

public interface OverlayVisiblePreferencesRepository {
    public fun observeOverlayVisible(): Flow<Boolean>

    public suspend fun saveOverlayVisible(visible: Boolean)
}
