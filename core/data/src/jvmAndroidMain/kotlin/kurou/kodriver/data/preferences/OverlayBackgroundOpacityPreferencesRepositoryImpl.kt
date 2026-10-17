package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository

internal class OverlayBackgroundOpacityPreferencesRepositoryImpl(
    private val dataStore: DataStore<OverlayBackgroundOpacityPreferences>,
) : OverlayBackgroundOpacityPreferencesRepository {
    // 保存値とは別の一時値をオーバーレイと共有する。null で解除した時点の最新の保存値へ戻す。
    private val previewOverlayBackgroundOpacity = MutableStateFlow<Int?>(null)

    override fun observeOverlayBackgroundOpacity(): Flow<Int> =
        combine(
            dataStore.observeProperty { it.opacity },
            previewOverlayBackgroundOpacity,
        ) { saved, preview -> preview ?: saved }

    override fun setPreviewOverlayBackgroundOpacity(opacity: Int?) {
        previewOverlayBackgroundOpacity.update { opacity }
    }

    override suspend fun saveOverlayBackgroundOpacity(opacity: Int) {
        dataStore.saveProperty(opacity) { prefs, value -> prefs.copy(opacity = value) }
    }
}
