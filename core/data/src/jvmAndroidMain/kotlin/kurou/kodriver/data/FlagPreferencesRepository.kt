package kurou.kodriver.data

import kurou.kodriver.data.datasource.createFlagPreferencesDataStore
import kurou.kodriver.data.repository.FlagPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.FlagPreferencesRepository

fun createFlagPreferencesRepository(directory: String): FlagPreferencesRepository =
    FlagPreferencesRepositoryImpl(createFlagPreferencesDataStore(directory))
