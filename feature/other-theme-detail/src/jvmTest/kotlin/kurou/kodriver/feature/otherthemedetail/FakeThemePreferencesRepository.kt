package kurou.kodriver.feature.otherthemedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository

internal class FakeThemePreferencesRepository(
    initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
) : ThemePreferencesRepository {
    private val state = MutableStateFlow(initialThemeMode)

    override fun observeThemeMode(): Flow<ThemeMode> = state

    override suspend fun saveThemeMode(themeMode: ThemeMode) {
        state.update { themeMode }
    }
}
