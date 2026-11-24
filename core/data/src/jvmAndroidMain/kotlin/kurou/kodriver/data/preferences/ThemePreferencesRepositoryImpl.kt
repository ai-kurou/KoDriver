package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.exportimport.Exportable
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository

internal class ThemePreferencesRepositoryImpl(
    private val dataStore: DataStore<ThemePreferences>,
) : ThemePreferencesRepository,
    Exportable by DataStoreExportable(
        key = "theme",
        dataStore = dataStore,
        serializer = ThemePreferences.serializer(),
    ) {
    override fun observeThemeMode(): Flow<ThemeMode> = dataStore.observeProperty { ThemeMode.fromId(it.mode) }

    override suspend fun saveThemeMode(themeMode: ThemeMode) {
        dataStore.saveProperty(themeMode.id) { prefs, value -> prefs.copy(mode = value) }
    }
}
