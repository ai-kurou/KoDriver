package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository

/** AndroidAudioUsagePreferences Repository の永続化実装を生成する。 */
fun createAndroidAudioUsagePreferencesRepository(directory: String): AndroidAudioUsagePreferencesRepository =
    AndroidAudioUsagePreferencesRepositoryImpl(createAndroidAudioUsagePreferencesDataStore(directory))
