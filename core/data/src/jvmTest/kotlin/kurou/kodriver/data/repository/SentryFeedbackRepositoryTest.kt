package kurou.kodriver.data.repository

import io.sentry.Scope
import io.sentry.SentryOptions
import io.sentry.UserFeedback
import io.sentry.protocol.SentryId
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.model.FeedbackType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SentryFeedbackRepositoryTest {
    @Test
    fun `Sentryイベントを作成してUserFeedbackを送信する`() =
        runTest {
            val sentryId = SentryId("0123456789abcdef0123456789abcdef")
            var capturedMessage: String? = null
            var capturedFeedback: UserFeedback? = null
            val scope = Scope(SentryOptions())
            val repository =
                SentryFeedbackRepository(
                    captureMessage = { message, configureScope ->
                        capturedMessage = message
                        configureScope.run(scope)
                        sentryId
                    },
                    captureUserFeedback = { capturedFeedback = it },
                )

            val result =
                repository.send(
                    Feedback(
                        type = FeedbackType.FeatureRequest,
                        message = "要望です",
                        email = "user@example.com",
                        name = "Kurou",
                        includesDiagnostics = true,
                    ),
                )

            assertTrue(result.isSuccess)
            assertEquals("User feedback submitted", capturedMessage)
            assertEquals("feature_request", scope.tags["feedback.type"])
            val context = scope.contexts.get("kodriver.feedback") as Map<*, *>
            assertEquals(true, context["includesDiagnostics"])
            assertEquals(sentryId, capturedFeedback?.eventId)
            assertEquals("要望です", capturedFeedback?.comments)
            assertEquals("user@example.com", capturedFeedback?.email)
            assertEquals("Kurou", capturedFeedback?.name)
        }

    @Test
    fun `Sentry送信に失敗したらResult failureを返す`() =
        runTest {
            val repository =
                SentryFeedbackRepository(
                    captureMessage = { _, _ -> error("failed") },
                )

            val result =
                repository.send(
                    Feedback(
                        type = FeedbackType.Other,
                        message = "本文",
                    ),
                )

            assertTrue(result.isFailure)
            assertEquals("failed", result.exceptionOrNull()?.message)
        }
}
