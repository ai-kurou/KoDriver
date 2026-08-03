package kurou.kodriver.data.repository

import io.sentry.ScopeCallback
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.UserFeedback
import io.sentry.protocol.SentryId
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.repository.FeedbackRepository

internal class SentryFeedbackRepository(
    private val captureMessage: (String, ScopeCallback) -> SentryId = Sentry::captureMessage,
    private val captureUserFeedback: (UserFeedback) -> Unit = Sentry::captureUserFeedback,
) : FeedbackRepository {
    override suspend fun send(feedback: Feedback): Result<Unit> =
        runCatching {
            val sentryId =
                captureMessage("User feedback submitted") { scope ->
                    scope.level = SentryLevel.INFO
                    scope.setTag("feedback.type", feedback.type.tagValue)
                    scope.setContexts(
                        "kodriver.feedback",
                        mapOf("includesDiagnostics" to feedback.includesDiagnostics),
                    )
                }
            captureUserFeedback(
                UserFeedback(sentryId).apply {
                    comments = feedback.message
                    feedback.email?.let { email = it }
                    feedback.name?.let { name = it }
                },
            )
        }
}
