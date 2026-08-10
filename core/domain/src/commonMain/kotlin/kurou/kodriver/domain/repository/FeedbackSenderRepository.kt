package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.Feedback

interface FeedbackSenderRepository {
    suspend fun send(feedback: Feedback): Result<Unit>
}
