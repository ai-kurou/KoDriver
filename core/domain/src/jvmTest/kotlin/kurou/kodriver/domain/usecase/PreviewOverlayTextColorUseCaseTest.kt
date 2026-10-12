package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kotlin.test.Test

class PreviewOverlayTextColorUseCaseTest {
    private val repository: OverlayTextColorPreferencesRepository = mockk()

    @Test
    fun `オーバーレイ文字色のプレビューを設定して解除できる`() {
        every { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) } returns Unit
        every { repository.setPreviewOverlayTextColor(null) } returns Unit
        val useCase = PreviewOverlayTextColorUseCase(repository)

        useCase(0xFFFFFFFFL)
        useCase(null)

        verify(exactly = 1) { repository.setPreviewOverlayTextColor(0xFFFFFFFFL) }
        verify(exactly = 1) { repository.setPreviewOverlayTextColor(null) }
        confirmVerified(repository)
    }
}
