package kurou.kodriver.domain.usecase

import kotlinx.coroutines.CancellationException
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository
import kurou.kodriver.domain.repository.FeedbackSenderRepository
import kotlin.time.Clock

class SendFeedbackUseCase(
    private val repository: FeedbackSenderRepository,
    private val cooldownRepository: FeedbackCooldownPreferencesRepository,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    suspend operator fun invoke(feedback: Feedback): Result<Unit> {
        val normalizedFeedback =
            feedback.copy(
                message = feedback.message.trim(),
                email = feedback.email?.trim()?.takeIf { it.isNotEmpty() },
                name = feedback.name?.trim()?.takeIf { it.isNotEmpty() },
            )
        if (normalizedFeedback.message.isEmpty()) {
            return Result.failure(IllegalArgumentException("Feedback message must not be blank."))
        }
        val result = repository.send(normalizedFeedback)
        if (result.isSuccess) {
            saveCooldownTimestamp()
        }
        return result
    }

    private suspend fun saveCooldownTimestamp() {
        try {
            cooldownRepository.saveLastFeedbackSentAtEpochMillis(currentTimeMs())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 送信自体は成功しているため、クールダウン記録の失敗で送信結果を失敗扱いにしない。
        }
    }
}
