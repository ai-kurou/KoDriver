package kurou.kodriver.data

import kurou.kodriver.data.datasource.createGt7Ps5RemainingFuelLapsPreferencesDataStore
import kurou.kodriver.data.repository.Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

fun createGt7Ps5RemainingFuelLapsPreferencesRepository(
    directory: String,
): Gt7Ps5RemainingFuelLapsPreferencesRepository =
    Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl(
        dataStore = createGt7Ps5RemainingFuelLapsPreferencesDataStore(directory),
    )
