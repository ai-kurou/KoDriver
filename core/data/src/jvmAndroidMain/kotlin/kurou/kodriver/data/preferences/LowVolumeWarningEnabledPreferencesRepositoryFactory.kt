package kurou.kodriver.data.preferences

/** LowVolumeWarningEnabledPreferences Repository の永続化実装を生成する。 */
internal fun createLowVolumeWarningEnabledPreferencesRepository(
    directory: String,
): LowVolumeWarningEnabledPreferencesRepositoryImpl =
    LowVolumeWarningEnabledPreferencesRepositoryImpl(createLowVolumeWarningEnabledPreferencesDataStore(directory))
