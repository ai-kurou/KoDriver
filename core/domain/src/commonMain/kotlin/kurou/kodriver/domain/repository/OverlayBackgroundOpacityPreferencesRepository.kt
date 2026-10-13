package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface OverlayBackgroundOpacityPreferencesRepository {
    fun observeOverlayBackgroundOpacity(): Flow<Int>

    /** 保存せずにプレビューを配信する。null で保存値に戻す。 */
    fun setPreviewOverlayBackgroundOpacity(opacity: Int?)

    suspend fun saveOverlayBackgroundOpacity(opacity: Int)
}
