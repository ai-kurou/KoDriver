package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface OverlayTextColorPreferencesRepository {
    public fun observeOverlayTextColor(): Flow<Long>

    /** 保存せずにプレビューを配信する。null で保存値に戻す。 */
    public fun setPreviewOverlayTextColor(argb: Long?)

    public suspend fun saveOverlayTextColor(argb: Long)
}
