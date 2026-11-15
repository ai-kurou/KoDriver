package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository

/**
 * ReadoutStartSoundPreferences Repository の永続化実装を生成する。
 */
internal fun createReadoutStartSoundPreferencesRepository(directory: String): ReadoutStartSoundPreferencesRepository =
    ReadoutStartSoundPreferencesRepositoryImpl(createReadoutStartSoundPreferencesDataStore(directory))
