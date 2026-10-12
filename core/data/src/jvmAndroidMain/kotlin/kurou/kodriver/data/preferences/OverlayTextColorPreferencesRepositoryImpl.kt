package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository

internal class OverlayTextColorPreferencesRepositoryImpl(
    private val dataStore: DataStore<OverlayTextColorPreferences>,
) : OverlayTextColorPreferencesRepository {
    private val previewOverlayTextColor = MutableStateFlow<Long?>(null)

    override fun observeOverlayTextColor(): Flow<Long> =
        combine(
            dataStore.observeProperty { it.argb },
            previewOverlayTextColor,
        ) { saved, preview -> preview ?: saved }

    override fun setPreviewOverlayTextColor(argb: Long?) {
        previewOverlayTextColor.update { argb }
    }

    override suspend fun saveOverlayTextColor(argb: Long) {
        dataStore.saveProperty(argb) { prefs, value -> prefs.copy(argb = value) }
    }
}
