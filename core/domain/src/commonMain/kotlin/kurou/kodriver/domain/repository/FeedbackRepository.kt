package kurou.kodriver.domain.repository

import kurou.kodriver.core.model.Feedback

interface FeedbackRepository {
    suspend fun send(feedback: Feedback): Result<Unit>
}
