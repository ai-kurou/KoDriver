package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.repository.FeedbackRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendFeedbackUseCaseTest {
    private val repository = mockk<FeedbackRepository>()
    private val useCase = SendFeedbackUseCase(repository)

    @Test
    fun `入力値を正規化してRepositoryへ送信する`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)

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
            coVerify {
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
        }

    @Test
    fun `任意項目が空文字ならnullとして送信する`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)

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
            coVerify {
                repository.send(
                    Feedback(
                        type = FeedbackType.Other,
                        message = "本文",
                        email = null,
                        name = null,
                    ),
                )
            }
        }

    @Test
    fun `本文が空なら失敗してRepositoryへ送信しない`() =
        runTest {
            val result = useCase(Feedback(type = FeedbackType.Question, message = "  "))

            assertTrue(result.isFailure)
            assertEquals("Feedback message must not be blank.", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { repository.send(any()) }
        }
}
