package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository

/**
 * SoundVolumePreferences Repository の永続化実装を生成する。
 */
fun createSoundVolumePreferencesRepository(directory: String): SoundVolumePreferencesRepository =
    SoundVolumePreferencesRepositoryImpl(createSoundVolumePreferencesDataStore(directory))
