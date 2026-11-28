package kurou.kodriver.domain.feedback.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.feedback.model.FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT
import kurou.kodriver.domain.feedback.repository.FeedbackCooldownPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CanSendFeedbackUseCaseTest {
    private val repository: FeedbackCooldownPreferencesRepository = mockk()

    @Test
    fun `未送信の場合は送信可能`() =
        runTest {
            every { repository.lastFeedbackSentAtEpochMillis() } returns flowOf(null)
            val useCase = CanSendFeedbackUseCase(repository, currentTimeMs = { 1_000_000L })

            assertTrue(useCase().first())
            verify(exactly = 1) { repository.lastFeedbackSentAtEpochMillis() }
            confirmVerified(repository)
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
            verify(exactly = 1) { repository.lastFeedbackSentAtEpochMillis() }
            confirmVerified(repository)
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
            verify(exactly = 1) { repository.lastFeedbackSentAtEpochMillis() }
            confirmVerified(repository)
        }

    @Test
    fun `購読中にクールダウン期間が経過すると送信可能に切り替わる`() =
        runTest {
            val lastSentAt = 1_000_000L
            every { repository.lastFeedbackSentAtEpochMillis() } returns MutableStateFlow(lastSentAt)
            val useCase = CanSendFeedbackUseCase(repository, currentTimeMs = { lastSentAt + currentTime })
            val results = mutableListOf<Boolean>()
            backgroundScope.launch { useCase().collect { results.add(it) } }

            advanceTimeBy(FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT)
            assertEquals(listOf(false), results)

            runCurrent()
            assertEquals(listOf(false, true), results)
            verify(exactly = 1) { repository.lastFeedbackSentAtEpochMillis() }
            confirmVerified(repository)
        }

    @Test
    fun `待機が遅れても現在時刻が期間を過ぎていれば再確認で送信可能になる`() =
        runTest {
            val lastSentAt = 1_000_000L
            var now = lastSentAt
            every { repository.lastFeedbackSentAtEpochMillis() } returns MutableStateFlow(lastSentAt)
            val useCase = CanSendFeedbackUseCase(repository, currentTimeMs = { now })
            val results = mutableListOf<Boolean>()
            backgroundScope.launch { useCase().collect { results.add(it) } }
            runCurrent()

            // 端末のスリープで、待機時間より実時刻が大きく進んだ状況を再現する。
            now = lastSentAt + FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT
            advanceTimeBy(60_000L)
            runCurrent()

            assertEquals(listOf(false, true), results)
            verify(exactly = 1) { repository.lastFeedbackSentAtEpochMillis() }
            confirmVerified(repository)
        }

    @Test
    fun `待機中に再送信されると新しい送信時刻からクールダウンをやり直す`() =
        runTest {
            val startAt = 1_000_000L
            val lastSentAt = MutableStateFlow(startAt)
            every { repository.lastFeedbackSentAtEpochMillis() } returns lastSentAt
            val useCase = CanSendFeedbackUseCase(repository, currentTimeMs = { startAt + currentTime })
            val results = mutableListOf<Boolean>()
            backgroundScope.launch { useCase().collect { results.add(it) } }

            advanceTimeBy(60_000L)
            lastSentAt.value = startAt + 60_000L
            advanceTimeBy(FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT)
            assertEquals(listOf(false), results)

            runCurrent()
            assertEquals(listOf(false, true), results)
            verify(exactly = 1) { repository.lastFeedbackSentAtEpochMillis() }
            confirmVerified(repository)
        }
}
