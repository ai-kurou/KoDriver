package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

/**
 * LmuWindowsFlagReadoutTextPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsFlagReadoutTextPreferencesRepository(
    directory: String,
): LmuWindowsFlagReadoutTextPreferencesRepository =
    LmuWindowsFlagReadoutTextPreferencesRepositoryImpl(
        createLmuWindowsFlagReadoutTextPreferencesDataStore(directory),
    )
