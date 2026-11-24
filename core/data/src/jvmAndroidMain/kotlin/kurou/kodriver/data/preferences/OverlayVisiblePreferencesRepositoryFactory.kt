package kurou.kodriver.data.preferences

/**
 * OverlayVisiblePreferences Repository の永続化実装を生成する。
 */
internal fun createOverlayVisiblePreferencesRepository(directory: String): OverlayVisiblePreferencesRepositoryImpl =
    OverlayVisiblePreferencesRepositoryImpl(createOverlayVisiblePreferencesDataStore(directory))
