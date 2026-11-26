package kurou.kodriver.data.preferences

import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository

/**
 * MicrophoneCutQuietSoundPreferences Repository の永続化実装を生成する。
 */
internal fun createMicrophoneCutQuietSoundPreferencesRepository(
    directory: String,
): MicrophoneCutQuietSoundPreferencesRepository =
    MicrophoneCutQuietSoundPreferencesRepositoryImpl(createMicrophoneCutQuietSoundPreferencesDataStore(directory))
