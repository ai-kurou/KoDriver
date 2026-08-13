package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository

/**
 * Gt7Ps5MyBestLapPreferences Repository の永続化実装を生成する。
 */
fun createGt7Ps5MyBestLapPreferencesRepository(directory: String): Gt7Ps5MyBestLapPreferencesRepository =
    Gt7Ps5MyBestLapPreferencesRepositoryImpl(createGt7Ps5MyBestLapPreferencesDataStore(directory))
