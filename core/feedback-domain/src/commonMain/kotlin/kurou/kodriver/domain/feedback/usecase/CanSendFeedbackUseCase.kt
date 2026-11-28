package kurou.kodriver.domain.feedback.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import kurou.kodriver.domain.feedback.model.FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT
import kurou.kodriver.domain.feedback.repository.FeedbackCooldownPreferencesRepository
import kotlin.time.Clock

public class CanSendFeedbackUseCase(
    private val repository: FeedbackCooldownPreferencesRepository,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    /** 送信可否を流す。クールダウン中は `false` を流し、購読中に期間が経過した時点で `true` を流す。 */
    @OptIn(ExperimentalCoroutinesApi::class)
    public operator fun invoke(): Flow<Boolean> =
        repository
            .lastFeedbackSentAtEpochMillis()
            .transformLatest { lastSentAtEpochMillis ->
                if (lastSentAtEpochMillis != null) {
                    var remainingMillis = remainingCooldownMillis(lastSentAtEpochMillis)
                    while (remainingMillis > 0) {
                        emit(false)
                        // delay は端末のスリープ中に進まないことがあるため、一定間隔で現在時刻を確認し直す。
                        delay(minOf(remainingMillis, RECHECK_INTERVAL_MILLIS))
                        remainingMillis = remainingCooldownMillis(lastSentAtEpochMillis)
                    }
                }
                emit(true)
            }.distinctUntilChanged()

    private fun remainingCooldownMillis(lastSentAtEpochMillis: Long): Long =
        lastSentAtEpochMillis + FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT - currentTimeMs()

    private companion object {
        const val RECHECK_INTERVAL_MILLIS = 60_000L
    }
}
