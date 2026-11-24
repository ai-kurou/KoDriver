package kurou.kodriver.data.preferences

/**
 * QueuePreferences Repository の永続化実装を生成する。
 */
internal fun createQueuePreferencesRepository(directory: String): QueuePreferencesRepositoryImpl =
    QueuePreferencesRepositoryImpl(createQueuePreferencesDataStore(directory))
