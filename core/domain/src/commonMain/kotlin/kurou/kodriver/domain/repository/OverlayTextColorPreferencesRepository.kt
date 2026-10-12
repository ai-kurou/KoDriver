package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface OverlayTextColorPreferencesRepository {
    fun observeOverlayTextColor(): Flow<Long>

    suspend fun saveOverlayTextColor(argb: Long)
}
