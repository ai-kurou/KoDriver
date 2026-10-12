package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kotlin.test.Test

class SaveOverlayTextColorUseCaseTest {
    private val repository: OverlayTextColorPreferencesRepository = mockk()

    @Test
    fun `オーバーレイ文字色を保存できる`() =
        runTest {
            coEvery { repository.saveOverlayTextColor(0xFFFFFFFFL) } returns Unit
            SaveOverlayTextColorUseCase(repository)(0xFFFFFFFFL)

            coVerify(exactly = 1) { repository.saveOverlayTextColor(0xFFFFFFFFL) }
            confirmVerified(repository)
        }
}
