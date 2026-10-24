package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository

/** LowVolumeWarningEnabledPreferences Repository の永続化実装を生成する。 */
fun createLowVolumeWarningEnabledPreferencesRepository(
    directory: String,
): LowVolumeWarningEnabledPreferencesRepository =
    LowVolumeWarningEnabledPreferencesRepositoryImpl(createLowVolumeWarningEnabledPreferencesDataStore(directory))
