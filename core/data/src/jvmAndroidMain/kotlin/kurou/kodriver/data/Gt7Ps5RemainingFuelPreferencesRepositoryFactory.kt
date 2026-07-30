package kurou.kodriver.data

import kurou.kodriver.data.datasource.createGt7Ps5RemainingFuelPreferencesDataStore
import kurou.kodriver.data.repository.Gt7Ps5RemainingFuelPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository

fun createGt7Ps5RemainingFuelPreferencesRepository(
    directory: String,
): Gt7Ps5RemainingFuelPreferencesRepository =
    Gt7Ps5RemainingFuelPreferencesRepositoryImpl(
        dataStore = createGt7Ps5RemainingFuelPreferencesDataStore(directory),
    )
