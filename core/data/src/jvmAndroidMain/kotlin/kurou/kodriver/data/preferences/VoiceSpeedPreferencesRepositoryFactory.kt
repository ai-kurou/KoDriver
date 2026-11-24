package kurou.kodriver.data.preferences

/**
 * VoiceSpeedPreferences Repository の永続化実装を生成する。
 */
internal fun createVoiceSpeedPreferencesRepository(directory: String): VoiceSpeedPreferencesRepositoryImpl =
    VoiceSpeedPreferencesRepositoryImpl(createVoiceSpeedPreferencesDataStore(directory))
