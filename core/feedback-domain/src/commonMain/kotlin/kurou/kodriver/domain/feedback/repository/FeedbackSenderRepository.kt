package kurou.kodriver.domain.feedback.repository

import kurou.kodriver.domain.feedback.model.Feedback

public interface FeedbackSenderRepository {
    public suspend fun send(feedback: Feedback): Result<Unit>
}
