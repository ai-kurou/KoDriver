package kurou.kodriver.data

import kurou.kodriver.data.datasource.createQueuePreferencesDataStore
import kurou.kodriver.data.repository.QueuePreferencesRepositoryImpl
import kurou.kodriver.domain.repository.QueuePreferencesRepository

/**
 * QueuePreferences Repository の永続化実装を生成する。
 */
fun createQueuePreferencesRepository(directory: String): QueuePreferencesRepository =
    QueuePreferencesRepositoryImpl(createQueuePreferencesDataStore(directory))
