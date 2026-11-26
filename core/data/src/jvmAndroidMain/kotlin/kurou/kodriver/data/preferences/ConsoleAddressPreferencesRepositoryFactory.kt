package kurou.kodriver.data.preferences

import kurou.kodriver.domain.serverconnection.repository.ConsoleAddressPreferencesRepository

/**
 * ConsoleAddressPreferences Repository の永続化実装を生成する。
 */
internal fun createConsoleAddressPreferencesRepository(directory: String): ConsoleAddressPreferencesRepository =
    ConsoleAddressPreferencesRepositoryImpl(createConsoleAddressDataStore(directory))
