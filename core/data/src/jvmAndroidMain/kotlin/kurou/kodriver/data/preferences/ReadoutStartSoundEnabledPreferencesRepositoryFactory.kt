package kurou.kodriver.data.preferences

/**
 * ReadoutStartSoundEnabledPreferences Repository の永続化実装を生成する。
 */
internal fun createReadoutStartSoundEnabledPreferencesRepository(
    directory: String,
): ReadoutStartSoundEnabledPreferencesRepositoryImpl =
    ReadoutStartSoundEnabledPreferencesRepositoryImpl(createReadoutStartSoundEnabledPreferencesDataStore(directory))
