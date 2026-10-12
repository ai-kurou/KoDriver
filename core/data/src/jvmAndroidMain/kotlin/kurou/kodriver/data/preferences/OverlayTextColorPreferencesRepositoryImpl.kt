package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository

internal class OverlayTextColorPreferencesRepositoryImpl(
    private val dataStore: DataStore<OverlayTextColorPreferences>,
) : OverlayTextColorPreferencesRepository {
    override fun observeOverlayTextColor(): Flow<Long> = dataStore.observeProperty { it.argb }

    override suspend fun saveOverlayTextColor(argb: Long) {
        dataStore.saveProperty(argb) { prefs, value -> prefs.copy(argb = value) }
    }
}
