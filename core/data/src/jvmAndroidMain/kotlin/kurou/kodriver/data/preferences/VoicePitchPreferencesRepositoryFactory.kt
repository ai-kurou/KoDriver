package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository

/**
 * VoicePitchPreferences Repository の永続化実装を生成する。
 */
internal fun createVoicePitchPreferencesRepository(directory: String): VoicePitchPreferencesRepository =
    VoicePitchPreferencesRepositoryImpl(createVoicePitchPreferencesDataStore(directory))
