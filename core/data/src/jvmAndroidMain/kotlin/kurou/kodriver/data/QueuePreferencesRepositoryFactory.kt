package kurou.kodriver.data

import kurou.kodriver.data.datasource.createQueuePreferencesDataStore
import kurou.kodriver.data.repository.QueuePreferencesRepositoryImpl
import kurou.kodriver.domain.repository.QueuePreferencesRepository

fun createQueuePreferencesRepository(directory: String): QueuePreferencesRepository =
    QueuePreferencesRepositoryImpl(createQueuePreferencesDataStore(directory))
