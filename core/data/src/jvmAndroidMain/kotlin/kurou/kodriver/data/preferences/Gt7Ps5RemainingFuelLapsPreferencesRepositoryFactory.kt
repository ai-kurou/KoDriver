package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

/**
 * Gt7Ps5RemainingFuelLapsPreferences Repository の永続化実装を生成する。
 */
fun createGt7Ps5RemainingFuelLapsPreferencesRepository(
    directory: String,
): Gt7Ps5RemainingFuelLapsPreferencesRepository =
    Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl(
        dataStore = createGt7Ps5RemainingFuelLapsPreferencesDataStore(directory),
    )
