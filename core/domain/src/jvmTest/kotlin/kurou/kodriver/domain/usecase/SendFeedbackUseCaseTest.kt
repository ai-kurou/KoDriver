package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.Feedback
import kurou.kodriver.core.model.FeedbackType
import kurou.kodriver.domain.repository.FeedbackRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendFeedbackUseCaseTest {
    @MockK
    private lateinit var repository: FeedbackRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `入力値を正規化してRepositoryへ送信する`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)
            val useCase = SendFeedbackUseCase(repository)

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
            val useCase = SendFeedbackUseCase(repository)

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
            val useCase = SendFeedbackUseCase(repository)

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
            val useCase = SendFeedbackUseCase(repository)

            val result = useCase(Feedback(type = FeedbackType.Question, message = "  "))

            assertTrue(result.isFailure)
            assertEquals("Feedback message must not be blank.", result.exceptionOrNull()?.message)
            coVerify(exactly = 0) { repository.send(any()) }
            confirmVerified(repository)
        }
}
