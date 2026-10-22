package kurou.kodriver.core.lmuwindowsdata.preferences

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository

/**
 * LmuWindowsTyreWearPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsTyreWearPreferencesRepository(directory: String): LmuWindowsTyreWearPreferencesRepository =
    LmuWindowsTyreWearPreferencesRepositoryImpl(
        createLmuWindowsTyreWearPreferencesDataStore(directory),
    )
