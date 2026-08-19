package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository

/**
 * ReadoutStartSoundEnabledPreferences Repository の永続化実装を生成する。
 */
fun createReadoutStartSoundEnabledPreferencesRepository(
    directory: String,
): ReadoutStartSoundEnabledPreferencesRepository =
    ReadoutStartSoundEnabledPreferencesRepositoryImpl(createReadoutStartSoundEnabledPreferencesDataStore(directory))
