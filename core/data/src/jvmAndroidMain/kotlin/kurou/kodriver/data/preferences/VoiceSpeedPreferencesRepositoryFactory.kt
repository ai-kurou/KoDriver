package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository

/**
 * VoiceSpeedPreferences Repository の永続化実装を生成する。
 */
fun createVoiceSpeedPreferencesRepository(directory: String): VoiceSpeedPreferencesRepository =
    VoiceSpeedPreferencesRepositoryImpl(createVoiceSpeedPreferencesDataStore(directory))
