package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.ThemePreferencesRepository

/**
 * ThemePreferences Repository の永続化実装を生成する。
 */
fun createThemePreferencesRepository(directory: String): ThemePreferencesRepository =
    ThemePreferencesRepositoryImpl(createThemePreferencesDataStore(directory))
