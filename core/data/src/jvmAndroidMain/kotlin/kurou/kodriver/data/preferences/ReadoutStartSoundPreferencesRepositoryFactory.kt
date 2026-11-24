package kurou.kodriver.data.preferences

/**
 * ReadoutStartSoundPreferences Repository の永続化実装を生成する。
 */
internal fun createReadoutStartSoundPreferencesRepository(
    directory: String,
): ReadoutStartSoundPreferencesRepositoryImpl =
    ReadoutStartSoundPreferencesRepositoryImpl(createReadoutStartSoundPreferencesDataStore(directory))
