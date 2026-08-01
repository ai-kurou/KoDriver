package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsTyreWearPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsTyreWearPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository

/**
 * LmuWindowsTyreWearPreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsTyreWearPreferencesRepository(
    directory: String,
): LmuWindowsTyreWearPreferencesRepository =
    LmuWindowsTyreWearPreferencesRepositoryImpl(
        createLmuWindowsTyreWearPreferencesDataStore(directory),
    )
