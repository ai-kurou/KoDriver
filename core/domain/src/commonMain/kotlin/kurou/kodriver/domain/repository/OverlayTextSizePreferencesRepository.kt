package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverlayTextSize

interface OverlayTextSizePreferencesRepository {
    fun observeOverlayTextSize(): Flow<OverlayTextSize>

    /** 保存せずにプレビューを配信する。null で保存値に戻す。 */
    fun setPreviewOverlayTextSize(overlayTextSize: OverlayTextSize?)

    suspend fun saveOverlayTextSize(overlayTextSize: OverlayTextSize)
}
