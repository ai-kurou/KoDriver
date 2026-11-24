package kurou.kodriver.data.preferences

/**
 * OverlayTextSizePreferences Repository の永続化実装を生成する。
 */
internal fun createOverlayTextSizePreferencesRepository(directory: String): OverlayTextSizePreferencesRepositoryImpl =
    OverlayTextSizePreferencesRepositoryImpl(createOverlayTextSizePreferencesDataStore(directory))
