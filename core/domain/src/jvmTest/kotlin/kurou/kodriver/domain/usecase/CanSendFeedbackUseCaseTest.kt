package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT
import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanSendFeedbackUseCaseTest {
    private val repository: FeedbackCooldownPreferencesRepository = mockk()

    @Test
    fun `未送信の場合は送信可能`() =
        runTest {
            every { repository.lastFeedbackSentAtEpochMillis() } returns flowOf(null)
            val useCase = CanSendFeedbackUseCase(repository, currentTimeMs = { 1_000_000L })

            assertTrue(useCase().first())
        }

    @Test
    fun `クールダウン期間が経過していれば送信可能`() =
        runTest {
            val lastSentAt = 1_000_000L
            every { repository.lastFeedbackSentAtEpochMillis() } returns flowOf(lastSentAt)
            val useCase =
                CanSendFeedbackUseCase(
                    repository,
                    currentTimeMs = { lastSentAt + FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT },
                )

            assertTrue(useCase().first())
        }

    @Test
    fun `クールダウン期間中は送信不可`() =
        runTest {
            val lastSentAt = 1_000_000L
            every { repository.lastFeedbackSentAtEpochMillis() } returns flowOf(lastSentAt)
            val useCase =
                CanSendFeedbackUseCase(
                    repository,
                    currentTimeMs = { lastSentAt + FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT - 1 },
                )

            assertFalse(useCase().first())
        }
}
