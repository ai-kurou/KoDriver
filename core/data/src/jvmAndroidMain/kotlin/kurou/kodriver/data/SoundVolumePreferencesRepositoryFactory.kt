package kurou.kodriver.data

import kurou.kodriver.data.datasource.createSoundVolumePreferencesDataStore
import kurou.kodriver.data.repository.SoundVolumePreferencesRepositoryImpl
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository

/**
 * SoundVolumePreferences Repository の永続化実装を生成する。
 */
fun createSoundVolumePreferencesRepository(directory: String): SoundVolumePreferencesRepository =
    SoundVolumePreferencesRepositoryImpl(createSoundVolumePreferencesDataStore(directory))
