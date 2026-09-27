package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverlayTextSize

interface OverlayTextSizePreferencesRepository {
    fun observeOverlayTextSize(): Flow<OverlayTextSize>

    suspend fun saveOverlayTextSize(overlayTextSize: OverlayTextSize)
}
