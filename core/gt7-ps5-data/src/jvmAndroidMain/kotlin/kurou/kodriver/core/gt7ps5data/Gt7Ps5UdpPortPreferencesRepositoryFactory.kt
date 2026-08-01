package kurou.kodriver.core.gt7ps5data

import kurou.kodriver.core.gt7ps5data.datasource.createGt7Ps5UdpPortPreferencesDataStore
import kurou.kodriver.core.gt7ps5data.repository.Gt7Ps5UdpPortPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository

/**
 * Gt7Ps5UdpPortPreferences Repository の永続化実装を生成する。
 */
fun createGt7Ps5UdpPortPreferencesRepository(directory: String): Gt7Ps5UdpPortPreferencesRepository =
    Gt7Ps5UdpPortPreferencesRepositoryImpl(createGt7Ps5UdpPortPreferencesDataStore(directory))
