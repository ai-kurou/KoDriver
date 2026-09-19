package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kotlin.test.Test

class SaveOverlayTextSizeUseCaseTest {
    private val repository: OverlayTextSizePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `オーバーレイ文字サイズを保存できる`() =
        runTest {
            SaveOverlayTextSizeUseCase(repository)(OverlayTextSize.SMALL)

            coVerify(exactly = 1) { repository.saveOverlayTextSize(OverlayTextSize.SMALL) }
            confirmVerified(repository)
        }
}
