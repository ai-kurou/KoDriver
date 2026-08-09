package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.model.ThemeMode
import kurou.kodriver.data.model.ThemePreferences
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import kurou.kodriver.domain.repository.ThemePreferencesRepository

internal class ThemePreferencesRepositoryImpl(
    private val dataStore: DataStore<ThemePreferences>,
) : ThemePreferencesRepository {
    override fun observeThemeMode(): Flow<ThemeMode> =
        dataStore.data.map { ThemeMode.fromId(it.mode) ?: THEME_MODE_DEFAULT }

    override suspend fun saveThemeMode(themeMode: ThemeMode) {
        dataStore.updateData { it.copy(mode = themeMode.id) }
    }
}
