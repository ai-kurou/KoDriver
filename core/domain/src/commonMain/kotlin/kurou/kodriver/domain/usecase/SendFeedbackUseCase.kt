package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.repository.FeedbackSenderRepository

class SendFeedbackUseCase(
    private val repository: FeedbackSenderRepository,
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
        return repository.send(normalizedFeedback)
    }
}
