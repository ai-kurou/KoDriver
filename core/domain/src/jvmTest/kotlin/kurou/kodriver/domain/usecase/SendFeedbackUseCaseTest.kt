package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository
import kurou.kodriver.domain.repository.FeedbackSenderRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendFeedbackUseCaseTest {
    private val repository: FeedbackSenderRepository = mockk()
    private val cooldownRepository: FeedbackCooldownPreferencesRepository = mockk()

    private fun createUseCase(currentTimeMs: Long = 1_700_000_000_000L) =
        SendFeedbackUseCase(repository, cooldownRepository, currentTimeMs = { currentTimeMs })

    @Test
    fun `入力値を正規化してRepositoryへ送信する`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)
            coEvery { cooldownRepository.saveLastFeedbackSentAtEpochMillis(any()) } returns Unit
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
            confirmVerified(repository)
        }

    @Test
    fun `任意項目が空文字ならnullとして送信する`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)
            coEvery { cooldownRepository.saveLastFeedbackSentAtEpochMillis(any()) } returns Unit
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
            confirmVerified(repository)
        }

    @Test
    fun `添付されたテレメトリログの情報はそのままRepositoryへ送信する`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)
            coEvery { cooldownRepository.saveLastFeedbackSentAtEpochMillis(any()) } returns Unit
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
            confirmVerified(repository)
        }

    @Test
    fun `本文が空なら失敗してRepositoryへ送信しない`() =
        runTest {
            val useCase = createUseCase()

            val result = useCase(Feedback(type = FeedbackType.Question, message = "  "))

            assertTrue(result.isFailure)
            assertEquals("Feedback message must not be blank.", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { repository.send(any()) }
            confirmVerified(repository)
        }

    @Test
    fun `送信成功時は現在時刻をクールダウンRepositoryへ保存する`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)
            coEvery { cooldownRepository.saveLastFeedbackSentAtEpochMillis(any()) } returns Unit
            val useCase = createUseCase(currentTimeMs = 1_234_567_890L)

            useCase(Feedback(type = FeedbackType.Question, message = "本文"))

            coVerify(exactly = 1) { cooldownRepository.saveLastFeedbackSentAtEpochMillis(1_234_567_890L) }
        }

    @Test
    fun `送信失敗時はクールダウンRepositoryへ保存しない`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.failure(IllegalStateException("network error"))
            val useCase = createUseCase()

            val result = useCase(Feedback(type = FeedbackType.Question, message = "本文"))

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { cooldownRepository.saveLastFeedbackSentAtEpochMillis(any()) }
        }

    @Test
    fun `クールダウン保存に失敗しても送信結果は成功として返す`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)
            coEvery {
                cooldownRepository.saveLastFeedbackSentAtEpochMillis(any())
            } throws IllegalStateException("write error")
            val useCase = createUseCase()

            val result = useCase(Feedback(type = FeedbackType.Question, message = "本文"))

            assertTrue(result.isSuccess)
        }
}
