package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository

/**
 * FeedbackCooldownPreferences Repository の永続化実装を生成する。
 */
fun createFeedbackCooldownPreferencesRepository(directory: String): FeedbackCooldownPreferencesRepository =
    FeedbackCooldownPreferencesRepositoryImpl(createFeedbackCooldownDataStore(directory))
