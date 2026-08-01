package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsPitTimingPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsPitTimingPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

/**
 * LmuWindowsPitTimingPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsPitTimingPreferencesRepository(directory: String): LmuWindowsPitTimingPreferencesRepository =
    LmuWindowsPitTimingPreferencesRepositoryImpl(
        dataStore = createLmuWindowsPitTimingPreferencesDataStore(directory),
    )
