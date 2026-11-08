package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSpeechPlaybackActiveUseCaseTest {
    private val repository: SpeechPlaybackStateRepository = mockk()

    @Test
    fun `Repositoryの再生状態をそのまま返す`() {
        val state = MutableStateFlow(true)
        every { repository.isActive } returns state

        assertEquals(state, ObserveSpeechPlaybackActiveUseCase(repository)())
        verify(exactly = 1) { repository.isActive }
        confirmVerified(repository)
    }
}
