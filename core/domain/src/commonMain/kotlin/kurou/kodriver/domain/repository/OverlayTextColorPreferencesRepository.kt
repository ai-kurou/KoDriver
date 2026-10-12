package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface OverlayTextColorPreferencesRepository {
    fun observeOverlayTextColor(): Flow<Long>

    /** 保存せずにプレビューを配信する。null で保存値に戻す。 */
    fun setPreviewOverlayTextColor(argb: Long?)

    suspend fun saveOverlayTextColor(argb: Long)
}
