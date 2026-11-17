package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ThemeMode

public interface ThemePreferencesRepository {
    public fun observeThemeMode(): Flow<ThemeMode>

    public suspend fun saveThemeMode(themeMode: ThemeMode)
}
