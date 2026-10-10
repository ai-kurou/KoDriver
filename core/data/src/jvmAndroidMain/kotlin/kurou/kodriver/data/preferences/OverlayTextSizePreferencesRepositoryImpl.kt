package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository

internal class OverlayTextSizePreferencesRepositoryImpl(
    private val dataStore: DataStore<OverlayTextSizePreferences>,
) : OverlayTextSizePreferencesRepository {
    private val previewOverlayTextSize = MutableStateFlow<OverlayTextSize?>(null)

    override fun observeOverlayTextSize(): Flow<OverlayTextSize> =
        combine(
            dataStore.observeProperty { OverlayTextSize.fromId(it.size) },
            previewOverlayTextSize,
        ) { saved, preview -> preview ?: saved }

    override fun setPreviewOverlayTextSize(overlayTextSize: OverlayTextSize?) {
        previewOverlayTextSize.update { overlayTextSize }
    }

    override suspend fun saveOverlayTextSize(overlayTextSize: OverlayTextSize) {
        dataStore.saveProperty(overlayTextSize.id) { prefs, value -> prefs.copy(size = value) }
    }
}
