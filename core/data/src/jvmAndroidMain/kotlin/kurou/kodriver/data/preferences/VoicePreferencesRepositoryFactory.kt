package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.VoicePreferencesRepository

/**
 * VoicePreferences Repository の永続化実装を生成する。
 */
internal fun createVoicePreferencesRepository(directory: String): VoicePreferencesRepository =
    VoicePreferencesRepositoryImpl(createVoicePreferencesDataStore(directory))
