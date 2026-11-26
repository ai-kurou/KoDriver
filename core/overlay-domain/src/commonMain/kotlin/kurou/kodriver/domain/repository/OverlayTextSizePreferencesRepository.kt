package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverlayTextSize

public interface OverlayTextSizePreferencesRepository {
    public fun observeOverlayTextSize(): Flow<OverlayTextSize>

    /** 保存せずにプレビューを配信する。null で保存値に戻す。 */
    public fun setPreviewOverlayTextSize(overlayTextSize: OverlayTextSize?)

    public suspend fun saveOverlayTextSize(overlayTextSize: OverlayTextSize)
}
