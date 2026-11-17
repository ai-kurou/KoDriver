package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.Feedback

public interface FeedbackSenderRepository {
    public suspend fun send(feedback: Feedback): Result<Unit>
}
