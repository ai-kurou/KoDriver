package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface HapticFeedbackEnabledRepository {
    fun hapticFeedbackEnabled(): Flow<Boolean>

    suspend fun saveHapticFeedbackEnabled(enabled: Boolean)
}
