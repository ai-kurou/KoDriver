package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kotlin.test.Test

class PreviewOverlayTextSizeUseCaseTest {
    private val repository: OverlayTextSizePreferencesRepository = mockk()

    @Test
    fun `オーバーレイ文字サイズのプレビューを設定して解除できる`() {
        every { repository.setPreviewOverlayTextSize(OverlayTextSize.SMALL) } returns Unit
        every { repository.setPreviewOverlayTextSize(null) } returns Unit
        val useCase = PreviewOverlayTextSizeUseCase(repository)

        useCase(OverlayTextSize.SMALL)
        useCase(null)

        verify(exactly = 1) { repository.setPreviewOverlayTextSize(OverlayTextSize.SMALL) }
        verify(exactly = 1) { repository.setPreviewOverlayTextSize(null) }
        confirmVerified(repository)
    }
}
