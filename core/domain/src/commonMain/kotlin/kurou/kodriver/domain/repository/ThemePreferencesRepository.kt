package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.ThemeMode

interface ThemePreferencesRepository {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun saveThemeMode(themeMode: ThemeMode)
}
