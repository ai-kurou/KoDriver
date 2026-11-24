package kurou.kodriver.data.preferences

/**
 * ThemePreferences Repository の永続化実装を生成する。
 */
internal fun createThemePreferencesRepository(directory: String): ThemePreferencesRepositoryImpl =
    ThemePreferencesRepositoryImpl(createThemePreferencesDataStore(directory))
