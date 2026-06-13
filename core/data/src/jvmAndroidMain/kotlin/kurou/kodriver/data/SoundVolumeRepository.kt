package kurou.kodriver.data

import kurou.kodriver.data.datasource.createSoundVolumePreferencesDataStore
import kurou.kodriver.data.repository.SoundVolumeRepositoryImpl
import kurou.kodriver.domain.repository.SoundVolumeRepository

fun createSoundVolumeRepository(directory: String): SoundVolumeRepository =
    SoundVolumeRepositoryImpl(createSoundVolumePreferencesDataStore(directory))
