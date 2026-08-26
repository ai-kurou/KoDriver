package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository

internal class JvmHapticFeedbackEnabledRepository : HapticFeedbackEnabledRepository {
    override fun hapticFeedbackEnabled(): Flow<Boolean> = flowOf(false)

    override suspend fun saveHapticFeedbackEnabled(enabled: Boolean) = Unit
}
