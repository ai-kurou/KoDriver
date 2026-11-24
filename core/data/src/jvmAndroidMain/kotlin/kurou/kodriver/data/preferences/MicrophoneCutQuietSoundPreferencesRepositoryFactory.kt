package kurou.kodriver.data.preferences

/**
 * MicrophoneCutQuietSoundPreferences Repository の永続化実装を生成する。
 */
internal fun createMicrophoneCutQuietSoundPreferencesRepository(
    directory: String,
): MicrophoneCutQuietSoundPreferencesRepositoryImpl =
    MicrophoneCutQuietSoundPreferencesRepositoryImpl(createMicrophoneCutQuietSoundPreferencesDataStore(directory))
