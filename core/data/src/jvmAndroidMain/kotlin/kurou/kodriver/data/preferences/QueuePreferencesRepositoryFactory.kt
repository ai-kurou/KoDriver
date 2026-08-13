package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.QueuePreferencesRepository

/**
 * QueuePreferences Repository の永続化実装を生成する。
 */
fun createQueuePreferencesRepository(directory: String): QueuePreferencesRepository =
    QueuePreferencesRepositoryImpl(createQueuePreferencesDataStore(directory))
