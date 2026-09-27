package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface OverlayBackgroundOpacityPreferencesRepository {
    fun observeOverlayBackgroundOpacity(): Flow<Int>

    suspend fun saveOverlayBackgroundOpacity(opacity: Int)
}
