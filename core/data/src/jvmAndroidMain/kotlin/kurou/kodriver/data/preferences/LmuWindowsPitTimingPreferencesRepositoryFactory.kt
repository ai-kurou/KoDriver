package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

/**
 * LmuWindowsPitTimingPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsPitTimingPreferencesRepository(directory: String): LmuWindowsPitTimingPreferencesRepository =
    LmuWindowsPitTimingPreferencesRepositoryImpl(
        dataStore = createLmuWindowsPitTimingPreferencesDataStore(directory),
    )
