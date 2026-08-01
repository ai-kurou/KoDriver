package kurou.kodriver.data

import kurou.kodriver.data.datasource.createReadoutStartSoundPreferencesDataStore
import kurou.kodriver.data.repository.ReadoutStartSoundPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository

/**
 * ReadoutStartSoundPreferences Repository の永続化実装を生成する。
 */
fun createReadoutStartSoundPreferencesRepository(directory: String): ReadoutStartSoundPreferencesRepository =
    ReadoutStartSoundPreferencesRepositoryImpl(createReadoutStartSoundPreferencesDataStore(directory))
