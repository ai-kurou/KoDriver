package kurou.kodriver.data.preferences

/**
 * OverlayBackgroundOpacityPreferences Repository の永続化実装を生成する。
 */
internal fun createOverlayBackgroundOpacityPreferencesRepository(
    directory: String,
): OverlayBackgroundOpacityPreferencesRepositoryImpl =
    OverlayBackgroundOpacityPreferencesRepositoryImpl(createOverlayBackgroundOpacityPreferencesDataStore(directory))
