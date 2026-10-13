package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kotlin.test.Test

class PreviewOverlayBackgroundOpacityUseCaseTest {
    private val repository: OverlayBackgroundOpacityPreferencesRepository = mockk()

    @Test
    fun `オーバーレイ背景透明度のプレビューを設定して解除できる`() {
        every { repository.setPreviewOverlayBackgroundOpacity(80) } returns Unit
        every { repository.setPreviewOverlayBackgroundOpacity(null) } returns Unit
        val useCase = PreviewOverlayBackgroundOpacityUseCase(repository)

        useCase(80)
        useCase(null)

        verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(80) }
        verify(exactly = 1) { repository.setPreviewOverlayBackgroundOpacity(null) }
        confirmVerified(repository)
    }
}
