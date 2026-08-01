package kurou.kodriver.data

import kurou.kodriver.data.datasource.createConsoleAddressDataStore
import kurou.kodriver.data.repository.ConsoleAddressPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository

/**
 * ConsoleAddressPreferences Repository の永続化実装を生成する。
 */
fun createConsoleAddressPreferencesRepository(directory: String): ConsoleAddressPreferencesRepository =
    ConsoleAddressPreferencesRepositoryImpl(createConsoleAddressDataStore(directory))
