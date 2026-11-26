package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface OverlayBackgroundOpacityPreferencesRepository {
    public fun observeOverlayBackgroundOpacity(): Flow<Int>

    /** 保存せずにプレビューを配信する。null で保存値に戻す。 */
    public fun setPreviewOverlayBackgroundOpacity(opacity: Int?)

    public suspend fun saveOverlayBackgroundOpacity(opacity: Int)
}
