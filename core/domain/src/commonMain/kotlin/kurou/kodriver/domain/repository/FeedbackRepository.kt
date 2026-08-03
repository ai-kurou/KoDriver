package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.Feedback

interface FeedbackRepository {
    suspend fun send(feedback: Feedback): Result<Unit>
}
