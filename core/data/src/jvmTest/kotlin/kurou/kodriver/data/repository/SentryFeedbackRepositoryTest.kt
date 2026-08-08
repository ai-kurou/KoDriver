package kurou.kodriver.data.repository

import io.sentry.Hint
import io.sentry.Scope
import io.sentry.SentryOptions
import io.sentry.protocol.SentryId
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.model.FeedbackType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.sentry.protocol.Feedback as SentryFeedback

class SentryFeedbackRepositoryTest {
    @Test
    fun `Sentryにフィードバックを送信する`() =
        runTest {
            val sentryId = SentryId("0123456789abcdef0123456789abcdef")
            var capturedFeedback: SentryFeedback? = null
            var capturedHint: Hint? = null
            var captureFeedbackCount = 0
            val scope = Scope(SentryOptions())
            val repository =
                SentryFeedbackRepository(
                    captureFeedback = { sentryFeedback, hint, configureScope ->
                        captureFeedbackCount += 1
                        capturedFeedback = sentryFeedback
                        capturedHint = hint
                        configureScope.run(scope)
                        sentryId
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
            assertEquals(1, captureFeedbackCount)
            assertEquals("要望です", capturedFeedback?.message)
            assertEquals("user@example.com", capturedFeedback?.contactEmail)
            assertEquals("Kurou", capturedFeedback?.name)
            assertTrue(capturedHint != null)
            assertEquals("feature_request", scope.tags["feedback.type"])
            val context = scope.contexts.get("kodriver.feedback") as Map<*, *>
            assertEquals(true, context["includesDiagnostics"])
        }

    @Test
    fun `Sentry送信に失敗したらResult failureを返す`() =
        runTest {
            var captureFeedbackCount = 0
            val repository =
                SentryFeedbackRepository(
                    captureFeedback = { _, _, _ ->
                        captureFeedbackCount += 1
                        error("failed")
                    },
                )

            val result =
                repository.send(
                    Feedback(
                        type = FeedbackType.Other,
                        message = "本文",
                    ),
                )

            assertTrue(result.isFailure)
            assertEquals(1, captureFeedbackCount)
            assertEquals("failed", result.exceptionOrNull()?.message)
        }
}
