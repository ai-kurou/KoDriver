package kurou.kodriver.data.repository

import io.sentry.Hint
import io.sentry.ScopeCallback
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import kotlinx.coroutines.CancellationException
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.repository.FeedbackRepository
import io.sentry.protocol.Feedback as SentryFeedback

internal class SentryFeedbackRepository(
    private val captureFeedback: (SentryFeedback, Hint, ScopeCallback) -> SentryId = {
        sentryFeedback,
        hint,
        scopeCallback,
        ->
        Sentry.feedback().capture(sentryFeedback, hint, scopeCallback)
    },
) : FeedbackRepository {
    override suspend fun send(feedback: Feedback): Result<Unit> =
        try {
            captureFeedback(
                SentryFeedback(feedback.message).apply {
                    feedback.email?.let { contactEmail = it }
                    feedback.name?.let { name = it }
                },
                Hint(),
            ) { scope ->
                scope.setTag("feedback.type", feedback.type.tagValue)
                scope.setContexts(
                    "kodriver.feedback",
                    buildMap {
                        put("includesDiagnostics", feedback.includesDiagnostics)
                        feedback.telemetryLogId?.let { put("telemetryLogId", it) }
                        feedback.telemetryLogJson?.let { put("telemetryLogJson", it) }
                    },
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
