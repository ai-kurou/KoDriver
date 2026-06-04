package kurou.kodriver.data

import kurou.kodriver.data.datasource.createReadoutPreferencesDataStore
import kurou.kodriver.data.repository.ReadoutPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal fun createReadoutPreferencesRepository(directory: String): ReadoutPreferencesRepository =
    ReadoutPreferencesRepositoryImpl(createReadoutPreferencesDataStore(directory))
