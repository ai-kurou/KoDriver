package kurou.kodriver.data.preferences

/** AndroidAudioUsagePreferences Repository の永続化実装を生成する。 */
internal fun createAndroidAudioUsagePreferencesRepository(
    directory: String,
): AndroidAudioUsagePreferencesRepositoryImpl =
    AndroidAudioUsagePreferencesRepositoryImpl(createAndroidAudioUsagePreferencesDataStore(directory))
