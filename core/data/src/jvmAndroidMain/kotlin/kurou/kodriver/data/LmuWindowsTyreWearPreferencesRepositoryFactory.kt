package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsTyreWearPreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsTyreWearPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository

fun createLmuWindowsTyreWearPreferencesRepository(
    directory: String,
): LmuWindowsTyreWearPreferencesRepository =
    LmuWindowsTyreWearPreferencesRepositoryImpl(
        createLmuWindowsTyreWearPreferencesDataStore(directory),
    )
