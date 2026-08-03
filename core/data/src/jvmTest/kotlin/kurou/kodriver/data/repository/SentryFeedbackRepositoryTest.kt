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
            var captureMessageCount = 0
            var captureUserFeedbackCount = 0
            val scope = Scope(SentryOptions())
            val repository =
                SentryFeedbackRepository(
                    captureMessage = { message, configureScope ->
                        captureMessageCount += 1
                        capturedMessage = message
                        configureScope.run(scope)
                        sentryId
                    },
                    captureUserFeedback = {
                        captureUserFeedbackCount += 1
                        capturedFeedback = it
                    },
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
            assertEquals(1, captureMessageCount)
            assertEquals(1, captureUserFeedbackCount)
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
            var captureMessageCount = 0
            var captureUserFeedbackCount = 0
            val repository =
                SentryFeedbackRepository(
                    captureMessage = { _, _ ->
                        captureMessageCount += 1
                        error("failed")
                    },
                    captureUserFeedback = { captureUserFeedbackCount += 1 },
                )

            val result =
                repository.send(
                    Feedback(
                        type = FeedbackType.Other,
                        message = "本文",
                    ),
                )

            assertTrue(result.isFailure)
            assertEquals(1, captureMessageCount)
            assertEquals(0, captureUserFeedbackCount)
            assertEquals("failed", result.exceptionOrNull()?.message)
        }
}
