package kurou.kodriver.domain.feedback.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.feedback.model.FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT
import kurou.kodriver.domain.feedback.repository.FeedbackCooldownPreferencesRepository
import kotlin.time.Clock

public class CanSendFeedbackUseCase(
    private val repository: FeedbackCooldownPreferencesRepository,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    public operator fun invoke(): Flow<Boolean> =
        repository.lastFeedbackSentAtEpochMillis().map { lastSentAtEpochMillis ->
            lastSentAtEpochMillis == null ||
                currentTimeMs() - lastSentAtEpochMillis >= FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT
        }
}
