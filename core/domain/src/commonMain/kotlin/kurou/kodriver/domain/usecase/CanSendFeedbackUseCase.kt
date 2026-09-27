package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT
import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository
import kotlin.time.Clock

class CanSendFeedbackUseCase(
    private val repository: FeedbackCooldownPreferencesRepository,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    operator fun invoke(): Flow<Boolean> =
        repository.lastFeedbackSentAtEpochMillis().map { lastSentAtEpochMillis ->
            lastSentAtEpochMillis == null ||
                currentTimeMs() - lastSentAtEpochMillis >= FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT
        }
}
