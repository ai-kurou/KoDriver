package kurou.kodriver.data.preferences

/**
 * SoundVolumePreferences Repository の永続化実装を生成する。
 */
internal fun createSoundVolumePreferencesRepository(directory: String): SoundVolumePreferencesRepositoryImpl =
    SoundVolumePreferencesRepositoryImpl(createSoundVolumePreferencesDataStore(directory))
