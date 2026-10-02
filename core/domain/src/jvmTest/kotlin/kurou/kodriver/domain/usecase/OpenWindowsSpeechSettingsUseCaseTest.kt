package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import kotlin.test.Test

class OpenWindowsSpeechSettingsUseCaseTest {
    private val repository: SpeechSettingsSenderRepository = mockk()

    @Test
    fun `Windowsの音声設定を開く処理をRepositoryへ委譲する`() {
        every { repository.openWindowsSpeechSettings() } returns Unit
        val useCase = OpenWindowsSpeechSettingsUseCase(repository)

        useCase()

        verify(exactly = 1) { repository.openWindowsSpeechSettings() }
        confirmVerified(repository)
    }
}
