package kurou.kodriver.data.preferences

/**
 * VoicePitchPreferences Repository の永続化実装を生成する。
 */
internal fun createVoicePitchPreferencesRepository(directory: String): VoicePitchPreferencesRepositoryImpl =
    VoicePitchPreferencesRepositoryImpl(createVoicePitchPreferencesDataStore(directory))
