package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository

internal class OverlayTextSizePreferencesRepositoryImpl(
    private val dataStore: DataStore<OverlayTextSizePreferences>,
) : OverlayTextSizePreferencesRepository {
    override fun observeOverlayTextSize(): Flow<OverlayTextSize> =
        dataStore.observeProperty { OverlayTextSize.fromId(it.size) }

    override suspend fun saveOverlayTextSize(overlayTextSize: OverlayTextSize) {
        dataStore.saveProperty(overlayTextSize.id) { prefs, value -> prefs.copy(size = value) }
    }
}
