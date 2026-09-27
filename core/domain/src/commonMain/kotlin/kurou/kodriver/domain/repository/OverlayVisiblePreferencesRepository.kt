package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface OverlayVisiblePreferencesRepository {
    fun observeOverlayVisible(): Flow<Boolean>

    suspend fun saveOverlayVisible(visible: Boolean)
}
