package kurou.kodriver.data.preferences

/**
 * OverlayTextColorPreferences Repository の永続化実装を生成する。
 */
internal fun createOverlayTextColorPreferencesRepository(directory: String): OverlayTextColorPreferencesRepositoryImpl =
    OverlayTextColorPreferencesRepositoryImpl(createOverlayTextColorPreferencesDataStore(directory))
