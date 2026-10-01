package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository
import kurou.kodriver.domain.repository.FeedbackSenderRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SendFeedbackUseCaseTest {
    private val repository: FeedbackSenderRepository = mockk()
    private val cooldownRepository: FeedbackCooldownPreferencesRepository = mockk()

    private fun createUseCase(currentTimeMs: Long = 1_700_000_000_000L) =
        SendFeedbackUseCase(repository, cooldownRepository, currentTimeMs = { currentTimeMs })

    @Test
    fun `入力値を正規化してRepositoryへ送信する`() =
        runTest {
            coEvery {
                repository.send(
                    Feedback(
                        type = FeedbackType.BugReport,
                        message = "動作しません",
                        email = "user@example.com",
                        name = "Kurou",
                        includesDiagnostics = true,
                    ),
                )
            } returns Result.success(Unit)
            coEvery { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) } returns Unit
            val useCase = createUseCase()

            val result =
                useCase(
                    Feedback(
                        type = FeedbackType.BugReport,
                        message = "  動作しません  ",
                        email = "  user@example.com  ",
                        name = "  Kurou  ",
                        includesDiagnostics = true,
                    ),
                )

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                repository.send(
                    Feedback(
                        type = FeedbackType.BugReport,
                        message = "動作しません",
                        email = "user@example.com",
                        name = "Kurou",
                        includesDiagnostics = true,
                    ),
                )
            }
            coVerify(exactly = 1) { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) }
            confirmVerified(repository, cooldownRepository)
        }

    @Test
    fun `任意項目が空文字ならnullとして送信する`() =
        runTest {
            coEvery {
                repository.send(Feedback(type = FeedbackType.Other, message = "本文", email = null, name = null))
            } returns Result.success(Unit)
            coEvery { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) } returns Unit
            val useCase = createUseCase()

            val result =
                useCase(
                    Feedback(
                        type = FeedbackType.Other,
                        message = "本文",
                        email = "  ",
                        name = "",
                    ),
                )

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                repository.send(
                    Feedback(
                        type = FeedbackType.Other,
                        message = "本文",
                        email = null,
                        name = null,
                    ),
                )
            }
            coVerify(exactly = 1) { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) }
            confirmVerified(repository, cooldownRepository)
        }

    @Test
    fun `添付されたテレメトリログの情報はそのままRepositoryへ送信する`() =
        runTest {
            coEvery {
                repository.send(
                    Feedback(
                        type = FeedbackType.BugReport,
                        message = "本文",
                        telemetryLogId = 1L,
                        telemetryLogJson = """{"lapCount":1}""",
                    ),
                )
            } returns Result.success(Unit)
            coEvery { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) } returns Unit
            val useCase = createUseCase()

            val result =
                useCase(
                    Feedback(
                        type = FeedbackType.BugReport,
                        message = "本文",
                        telemetryLogId = 1L,
                        telemetryLogJson = """{"lapCount":1}""",
                    ),
                )

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                repository.send(
                    Feedback(
                        type = FeedbackType.BugReport,
                        message = "本文",
                        telemetryLogId = 1L,
                        telemetryLogJson = """{"lapCount":1}""",
                    ),
                )
            }
            coVerify(exactly = 1) { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) }
            confirmVerified(repository, cooldownRepository)
        }

    @Test
    fun `本文が空なら失敗してRepositoryへ送信しない`() =
        runTest {
            val useCase = createUseCase()

            val result = useCase(Feedback(type = FeedbackType.Question, message = "  "))

            assertTrue(result.isFailure)
            assertEquals("Feedback message must not be blank.", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { repository.send(Feedback(type = FeedbackType.Question, message = "  ")) }
            confirmVerified(repository)
        }

    @Test
    fun `送信成功時は現在時刻をクールダウンRepositoryへ保存する`() =
        runTest {
            coEvery { repository.send(Feedback(type = FeedbackType.Question, message = "本文")) } returns
                Result.success(Unit)
            coEvery { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_234_567_890L) } returns Unit
            val useCase = createUseCase(currentTimeMs = 1_234_567_890L)

            useCase(Feedback(type = FeedbackType.Question, message = "本文"))

            coVerify(exactly = 1) { repository.send(Feedback(type = FeedbackType.Question, message = "本文")) }
            coVerify(exactly = 1) { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_234_567_890L) }
            confirmVerified(repository, cooldownRepository)
        }

    @Test
    fun `送信失敗時はクールダウンRepositoryへ保存しない`() =
        runTest {
            coEvery { repository.send(Feedback(type = FeedbackType.Question, message = "本文")) } returns
                Result.failure(IllegalStateException("network error"))
            val useCase = createUseCase()

            val result = useCase(Feedback(type = FeedbackType.Question, message = "本文"))

            assertTrue(result.isFailure)
            coVerify(exactly = 1) { repository.send(Feedback(type = FeedbackType.Question, message = "本文")) }
            coVerify(exactly = 0) { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) }
            confirmVerified(repository, cooldownRepository)
        }

    @Test
    fun `クールダウン保存に失敗しても送信結果は成功として返す`() =
        runTest {
            coEvery { repository.send(Feedback(type = FeedbackType.Question, message = "本文")) } returns
                Result.success(Unit)
            val exception = IllegalStateException("write error")
            mockkStatic(Sentry::class)
            every { Sentry.captureException(exception) } returns SentryId.EMPTY_ID
            try {
                coEvery {
                    cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L)
                } throws exception
                val useCase = createUseCase()

                val result = useCase(Feedback(type = FeedbackType.Question, message = "本文"))

                assertTrue(result.isSuccess)
                coVerify(exactly = 1) { repository.send(Feedback(type = FeedbackType.Question, message = "本文")) }
                coVerify(exactly = 1) { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) }
                verify(exactly = 1) { Sentry.captureException(exception) }
                confirmVerified(repository, cooldownRepository, Sentry::class)
            } finally {
                unmockkStatic(Sentry::class)
            }
        }

    @Test
    fun `クールダウン保存のキャンセルは報告せず再スローする`() =
        runTest {
            val exception = CancellationException("cancelled")
            val feedback = Feedback(type = FeedbackType.Question, message = "本文")
            coEvery { repository.send(feedback) } returns Result.success(Unit)
            coEvery { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) } throws exception
            mockkStatic(Sentry::class)
            try {
                assertSame(exception, assertFailsWith<CancellationException> { createUseCase()(feedback) })
                coVerify(exactly = 1) { repository.send(feedback) }
                coVerify(exactly = 1) { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L) }
                verify(exactly = 0) { Sentry.captureException(exception) }
                confirmVerified(repository, cooldownRepository, Sentry::class)
            } finally {
                unmockkStatic(Sentry::class)
            }
        }
}
