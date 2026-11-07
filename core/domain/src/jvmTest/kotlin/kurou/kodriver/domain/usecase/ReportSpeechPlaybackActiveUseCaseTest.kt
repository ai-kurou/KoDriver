package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository
import kotlin.test.Test

class ReportSpeechPlaybackActiveUseCaseTest {
    private val repository: SpeechPlaybackStateRepository = mockk()

    @Test
    fun `シミュレーターIDと再生状態をRepositoryへ渡す`() {
        every { repository.update("lmu_windows", true) } returns Unit

        ReportSpeechPlaybackActiveUseCase(repository)("lmu_windows", true)

        verify(exactly = 1) { repository.update("lmu_windows", true) }
        confirmVerified(repository)
    }
}
